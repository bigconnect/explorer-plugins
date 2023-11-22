package io.bigconnect.web.actions.video;

import com.mware.core.model.longRunningProcess.LongRunningProcessQueueItemBase;
import com.mware.core.util.BcLogger;
import com.mware.core.util.BcLoggerFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CutVideoQueueItem extends LongRunningProcessQueueItemBase {
    private static final BcLogger LOGGER = BcLoggerFactory.getLogger(CutVideoQueueItem.class);
    public static final String NVR_CUT_VIDEO_TYPE = "cut-video";

    private String vertexId;
    private String start;
    private String end;
    private String edgeLabel;
    private String userId;
    private String workspaceId;
    private String[] authorizations;

    @Override
    public String getType() {
        return NVR_CUT_VIDEO_TYPE;
    }
}
