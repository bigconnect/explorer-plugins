package io.bigconnect.web.actions.video;

import com.google.inject.Inject;
import com.mware.bigconnect.ffmpeg.AVMediaInfo;
import com.mware.bigconnect.ffmpeg.AVUtils;
import com.mware.core.exception.BcException;
import com.mware.core.ingest.dataworker.ElementOrPropertyStatus;
import com.mware.core.model.Description;
import com.mware.core.model.Name;
import com.mware.core.model.clientapi.dto.VisibilityJson;
import com.mware.core.model.longRunningProcess.LongRunningProcessRepository;
import com.mware.core.model.longRunningProcess.LongRunningProcessWorker;
import com.mware.core.model.properties.BcSchema;
import com.mware.core.model.properties.types.PropertyMetadata;
import com.mware.core.model.schema.SchemaConstants;
import com.mware.core.model.user.UserRepository;
import com.mware.core.model.workQueue.Priority;
import com.mware.core.model.workQueue.WorkQueueRepository;
import com.mware.core.security.BcVisibility;
import com.mware.core.security.VisibilityTranslator;
import com.mware.core.user.User;
import com.mware.core.util.BcLogger;
import com.mware.core.util.BcLoggerFactory;
import com.mware.core.util.ClientApiConverter;
import com.mware.ge.*;
import com.mware.ge.values.storable.ByteArray;
import com.mware.ge.values.storable.DefaultStreamingPropertyValue;
import com.mware.ge.values.storable.StreamingPropertyValue;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Name("Cut video")
@Description("Cuts a video in-place")
public class CutVideoLongRunningProcessWorker extends LongRunningProcessWorker {
    private static final BcLogger LOGGER = BcLoggerFactory.getLogger(CutVideoLongRunningProcessWorker.class);
    private final static DateTimeFormatter HH_MM_SS = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final UserRepository userRepository;
    private final LongRunningProcessRepository longRunningProcessRepository;
    private final Graph graph;
    private final WorkQueueRepository workQueueRepository;
    private final VisibilityTranslator visibilityTranslator;

    @Inject
    public CutVideoLongRunningProcessWorker(
            UserRepository userRepository,
            LongRunningProcessRepository longRunningProcessRepository,
            Graph graph,
            WorkQueueRepository workQueueRepository,
            VisibilityTranslator visibilityTranslator
    ) {
        this.userRepository = userRepository;
        this.longRunningProcessRepository = longRunningProcessRepository;
        this.graph = graph;
        this.workQueueRepository = workQueueRepository;
        this.visibilityTranslator = visibilityTranslator;
    }


    @Override
    public boolean isHandled(JSONObject queueItem) {
        return queueItem.getString("type").equals(CutVideoQueueItem.NVR_CUT_VIDEO_TYPE);
    }

    @Override
    protected void processInternal(JSONObject config) {
        CutVideoQueueItem queueItem = ClientApiConverter.toClientApi(config.toString(), CutVideoQueueItem.class);

        try {
            User user = userRepository.findById(queueItem.getUserId());
            if (user == null) {
                throw new BcException(String.format("User with id %s not found.", queueItem.getUserId()));
            }

            longRunningProcessRepository.reportProgress(config, 0, "Cutting video");
            Authorizations authorizations = graph.createAuthorizations(queueItem.getAuthorizations());
            Vertex vertex = graph.getVertex(queueItem.getVertexId(), authorizations);

            StreamingPropertyValue spv = BcSchema.RAW.getPropertyValue(vertex);
            if (spv != null) {
                java.nio.file.Path tempFolder = createVideoFile(longRunningProcessRepository, queueItem, config, spv);

                longRunningProcessRepository.reportProgress(config, 0.75, "Creating new video object");
                VisibilityJson visibilityJson = VisibilityJson.updateVisibilitySourceAndAddWorkspaceId(
                        null,
                        Visibility.EMPTY.getVisibilityString(),
                        queueItem.getWorkspaceId()
                );
                BcVisibility bcVisibility = visibilityTranslator.toVisibility(visibilityJson);
                Metadata metadata = new PropertyMetadata(user, new VisibilityJson(), Visibility.EMPTY)
                        .createMetadata();
                VertexBuilder vb = graph.prepareVertex(bcVisibility.getVisibility(), SchemaConstants.CONCEPT_TYPE_THING);
                byte[] bytes = Files.readAllBytes(tempFolder.resolve("out.mp4"));

                StreamingPropertyValue spv2 = DefaultStreamingPropertyValue.create(new ByteArrayInputStream(bytes), ByteArray.class);
                BcSchema.RAW.setProperty(vb, spv2, metadata, Visibility.EMPTY);

                String oldTitle = BcSchema.TITLE.getFirstPropertyValue(vertex);
                LocalTime startTime = LocalTime.parse(queueItem.getStart(), HH_MM_SS);
                LocalTime endTime = LocalTime.parse(queueItem.getEnd(), HH_MM_SS);
                String intervalStr = String.format("(%s - %s)", startTime.format(HH_MM_SS), endTime.format(HH_MM_SS));
                if (!StringUtils.isEmpty(oldTitle)) {
                    BcSchema.TITLE.addPropertyValue(vb, "", String.format("%s EDIT %s", oldTitle, intervalStr), metadata, Visibility.EMPTY);
                } else {
                    BcSchema.TITLE.addPropertyValue(vb, "", String.format("%s EDIT %s", vb.getId(), intervalStr), metadata, Visibility.EMPTY);
                }

                BcSchema.FILE_NAME.addPropertyValue(vb, "", vb.getId() + ".mp4", Visibility.EMPTY);

                Vertex vertex2 = vb.save(authorizations);
                graph.flush();

                graph.prepareEdge(vertex, vertex2, queueItem.getEdgeLabel(), Visibility.EMPTY)
                        .save(authorizations);

                graph.flush();

                workQueueRepository.pushOnDwQueue(
                        vertex2,
                        "",
                        BcSchema.RAW.getPropertyName(),
                        queueItem.getWorkspaceId(),
                        null,
                        Priority.NORMAL,
                        ElementOrPropertyStatus.UPDATE,
                        null
                );

                longRunningProcessRepository.reportProgress(config, 1.0, "Finished");
                FileUtils.deleteQuietly(tempFolder.toFile());
            }
        } catch (Exception ex) {
            LOGGER.error("Could not cut video clip!", ex);
            config.put("error", ex.getMessage());
            longRunningProcessRepository.reportProgress(config, 1.0, "Error");
        }
    }

    private Path createVideoFile(
            LongRunningProcessRepository longRunningProcessRepository,
            CutVideoQueueItem queueItem,
            JSONObject config,
            StreamingPropertyValue spv
    ) throws Exception {
        Path tempFolder = Files.createTempDirectory("nvrcut-");

        longRunningProcessRepository.reportProgress(config, 0.25, "Writing video to disk");
        Path videoFile = Files.createFile(tempFolder.resolve(queueItem.getVertexId()));
        IOUtils.copyLarge(spv.getInputStream(), Files.newOutputStream(videoFile.toFile().toPath()));

        longRunningProcessRepository.reportProgress(config, 0.5, "Cutting video");
        FFmpegProbeResult probe = AVMediaInfo.probe(videoFile.toAbsolutePath().toString());
        boolean hasAudio = AVMediaInfo.hasAudioStream(probe);
        boolean hasVideo = AVMediaInfo.hasVideoStream(probe);

        LocalTime startTime = LocalTime.parse(queueItem.getStart(), DateTimeFormatter.ofPattern("HH:mm:ss"));
        LocalTime endTime = LocalTime.parse(queueItem.getEnd(), DateTimeFormatter.ofPattern("HH:mm:ss"));

        StringBuilder sb = new StringBuilder();
        if (hasVideo)
            sb.append(String.format("[0:v]trim=start=%s:end=%s,setpts=PTS-STARTPTS[0v];", startTime.toSecondOfDay(), endTime.toSecondOfDay()));
        if (hasAudio)
            sb.append(String.format("[0:a]atrim=start=%s:end=%s,asetpts=PTS-STARTPTS[0a];", startTime.toSecondOfDay(), endTime.toSecondOfDay()));

        sb.append("[0v][0a]concat=n=1");

        if (hasVideo)
            sb.append(":v=1");
        if (hasAudio)
            sb.append(":a=1");
        if (hasVideo)
            sb.append("[outv0]");
        if (hasAudio)
            sb.append("[outa0]");

        sb.append(';');

        java.nio.file.Path filterFile = Files.createFile(tempFolder.resolve("filter_0"));
        String filter_script = StringUtils.stripEnd(sb.toString(), ";");
        Files.write(filterFile, filter_script.getBytes());

        FFmpegBuilder builder = new FFmpegBuilder()
                .addInput(videoFile.toAbsolutePath().toString())
                .addExtraArgs("-filter_complex_script", filterFile.toAbsolutePath().toString());

        String outputFile = tempFolder.resolve("out.mp4").toAbsolutePath().toString();
        FFmpegOutputBuilder outputBuilder = builder.addOutput(outputFile);

        if (hasVideo)
            outputBuilder.addExtraArgs("-map", "[outv0]");
        if (hasAudio)
            outputBuilder.addExtraArgs("-map", "[outa0]");

        outputBuilder.done();

        FFmpegExecutor executor = new FFmpegExecutor(AVUtils.ffmpeg());
        FFmpegJob job = executor.createJob(builder);
        job.run();

        return tempFolder;
    }
}
