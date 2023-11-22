package io.bigconnect.web.actions.video;


import com.drew.lang.Iterables;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.mware.bigconnect.ffmpeg.AVUtils;
import com.mware.core.exception.BcException;
import com.mware.core.ingest.dataworker.ElementOrPropertyStatus;
import com.mware.core.model.Description;
import com.mware.core.model.Name;
import com.mware.core.model.clientapi.dto.VisibilityJson;
import com.mware.core.model.graph.GraphRepository;
import com.mware.core.model.longRunningProcess.LongRunningProcessRepository;
import com.mware.core.model.longRunningProcess.LongRunningProcessWorker;
import com.mware.core.model.properties.BcSchema;
import com.mware.core.model.properties.types.PropertyMetadata;
import com.mware.core.model.schema.SchemaConstants;
import com.mware.core.model.user.UserRepository;
import com.mware.core.model.workQueue.Priority;
import com.mware.core.model.workQueue.WorkQueueRepository;
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
import net.bramp.ffmpeg.job.FFmpegJob;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Name("Merge videos")
@Description("Merge selected videos")
public class MergeVideosLongRunningProcessWorker extends LongRunningProcessWorker {
    private static final BcLogger LOGGER = BcLoggerFactory.getLogger(MergeVideosLongRunningProcessWorker.class);
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";

    private final UserRepository userRepository;
    private final LongRunningProcessRepository longRunningProcessRepository;
    private final Graph graph;
    private final WorkQueueRepository workQueueRepository;
    private final GraphRepository graphRepository;
    private final VisibilityTranslator visibilityTranslator;

    @Inject
    public MergeVideosLongRunningProcessWorker(
            UserRepository userRepository,
            LongRunningProcessRepository longRunningProcessRepository,
            Graph graph,
            WorkQueueRepository workQueueRepository,
            GraphRepository graphRepository,
            VisibilityTranslator visibilityTranslator
    ) {
        this.userRepository = userRepository;
        this.longRunningProcessRepository = longRunningProcessRepository;
        this.graph = graph;
        this.workQueueRepository = workQueueRepository;
        this.graphRepository = graphRepository;
        this.visibilityTranslator = visibilityTranslator;
    }

    @Override
    public boolean isHandled(JSONObject queueItem) {
        return queueItem.getString("type").equals(MergeVideosQueueItem.NVR_MERGE_VIDEOS_TYPE);
    }

    @Override
    protected void processInternal(JSONObject config) {
        MergeVideosQueueItem queueItem = ClientApiConverter
                .toClientApi(config.toString(), MergeVideosQueueItem.class);

        try {
            User user = userRepository.findById(queueItem.getUserId());
            if (user == null) {
                throw new BcException(String.format("User with id %s not found.", queueItem.getUserId()));
            }

            longRunningProcessRepository.reportProgress(config, 0, "Starting");
            Authorizations authorizations = graph.createAuthorizations(queueItem.getAuthorizations());
            Path tempFolder = Files.createTempDirectory("nvrmerge-");
            longRunningProcessRepository.reportProgress(config, 0.25, "Writing video files to disk");
            List<Vertex> vertices = Iterables.toList(graph.getVertices(queueItem.getVertexIds(), authorizations));
            // the above command does not guarrantee order of returned vertices

            for (int i = 0; i < queueItem.getVertexIds().size(); i++) {
                String id = queueItem.getVertexIds().get(i);
                Vertex vertex = vertices.stream().filter(v -> id.equals(v.getId())).findAny()
                        .orElseThrow(() -> new BcException("Could not find vertex with id: "+id));
                StreamingPropertyValue spv = BcSchema.RAW.getPropertyValue(vertex);
                if (spv != null) {
                    String fileName = String.valueOf(LETTERS.charAt(i));
                    fileName = StringUtils.appendIfMissing(fileName, ".mp4");
                    Path videoFile = Files.createFile(tempFolder.resolve(fileName));
                    IOUtils.copyLarge(spv.getInputStream(), new FileOutputStream(videoFile.toFile()));
                }
            }

            List<Path> videoFiles = Files.list(tempFolder).filter(p -> p.toString().endsWith(".mp4"))
                    .sorted(Comparator.comparing(Path::toString))
                    .collect(Collectors.toList());

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < videoFiles.size(); i++) {
                String filePath = videoFiles.get(i).toAbsolutePath().toString();
                sb.append("file ").append(filePath).append("\n");
            }

            Path sourceFile = Files.createFile(tempFolder.resolve("files"));
            Files.write(sourceFile, sb.toString().getBytes());

            Path finalFile = tempFolder.resolve("final.mp4");
            FFmpegBuilder builder = new FFmpegBuilder() {
                @Override
                public List<String> build() {
                    ImmutableList.Builder<String> args = new ImmutableList.Builder<String>();
                    args.add("-y");
                    args.add("-v", "error");
                    args.add("-f", "concat");
                    args.add("-safe", "0");
                    args.add("-i", sourceFile.toAbsolutePath().toString());
                    args.add("-c", "copy");
                    args.add(finalFile.toAbsolutePath().toString());
                    return args.build();
                }
            };

            FFmpegExecutor executor = new FFmpegExecutor(AVUtils.ffmpeg());
            FFmpegJob job = executor.createJob(builder);
            job.run();

            String finalVertexId = createVertex(finalFile, queueItem.getTitle(), user, authorizations);
            config.put("finalId", finalVertexId);

            longRunningProcessRepository.reportProgress(config, 1.0, "Finished");
            FileUtils.deleteQuietly(tempFolder.toFile());
        } catch (Exception ex) {
            LOGGER.error("Could not merge videos", ex);
            config.put("error", ex.getMessage());
            longRunningProcessRepository.reportProgress(config, 1.0, "Error");
        }
    }

    private String createVertex(Path finalFile, String videoTitle, User user, Authorizations authorizations) {
        try {
            VertexBuilder vb = graph.prepareVertex(Visibility.EMPTY, SchemaConstants.CONCEPT_TYPE_VIDEO);
            Metadata metadata = new PropertyMetadata(user, new VisibilityJson(), Visibility.EMPTY)
                    .createMetadata();

            StreamingPropertyValue spv = DefaultStreamingPropertyValue.create(new FileInputStream(finalFile.toFile()), ByteArray.class);
            BcSchema.TITLE.addPropertyValue(vb, "", videoTitle, metadata, Visibility.EMPTY);
            BcSchema.RAW.setProperty(vb, spv, metadata, Visibility.EMPTY);
            BcSchema.FILE_NAME.addPropertyValue(vb, "", videoTitle + ".mp4", Visibility.EMPTY);
            Vertex vertex = vb.save(authorizations);
            workQueueRepository.pushOnDwQueue(
                    vertex,
                    "",
                    BcSchema.RAW.getPropertyName(),
                    null,
                    null,
                    Priority.NORMAL,
                    ElementOrPropertyStatus.UPDATE,
                    null
            );

            return vertex.getId();
        } catch (IOException e) {
            throw new BcException("Could not read video file", e);
        }
    }
}
