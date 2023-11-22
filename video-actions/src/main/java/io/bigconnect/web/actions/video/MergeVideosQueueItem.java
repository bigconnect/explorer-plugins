package io.bigconnect.web.actions.video;

import com.mware.core.model.longRunningProcess.LongRunningProcessQueueItemBase;
import com.mware.core.util.BcLogger;
import com.mware.core.util.BcLoggerFactory;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MergeVideosQueueItem extends LongRunningProcessQueueItemBase {
    private static final BcLogger LOGGER = BcLoggerFactory.getLogger(MergeVideosQueueItem.class);
    public static final String NVR_MERGE_VIDEOS_TYPE = "merge-videos";

    private String title;
    private List<String> vertexIds;
    private String userId;
    private String workspaceId;
    private String[] authorizations;

    @Override
    public String getType() {
        return NVR_MERGE_VIDEOS_TYPE;
    }
}
