package io.bigconnect.web.actions.video;

import com.mware.core.model.properties.SchemaProperties;
import com.mware.core.model.schema.SchemaConstants;
import com.mware.core.model.schema.SchemaContribution;
import com.mware.core.model.schema.SchemaFactory;
import com.mware.ge.values.storable.BooleanValue;

import static com.mware.ge.values.storable.Values.stringValue;

public class VideoSchemaContribution implements SchemaContribution {
    public static final String EDGE_LABEL_HAS_VIDEO = "hasVideo";

    @Override
    public boolean patchApplied(SchemaFactory schemaFactory) {
        return schemaFactory.getRelationship(EDGE_LABEL_HAS_VIDEO) != null;
    }

    @Override
    public void patchSchema(SchemaFactory schemaFactory) {
        if (schemaFactory.getRelationship(EDGE_LABEL_HAS_VIDEO) == null) {
            schemaFactory.newRelationship()
                    .label(EDGE_LABEL_HAS_VIDEO)
                    .source(schemaFactory.getConcept(SchemaConstants.CONCEPT_TYPE_THING))
                    .target(schemaFactory.getConcept(SchemaConstants.CONCEPT_TYPE_VIDEO))
                    .property(SchemaProperties.USER_VISIBLE.getPropertyName(), BooleanValue.TRUE)
                    .property(SchemaProperties.DISPLAY_NAME.getPropertyName(), stringValue("Has Video"))
                    .save();
        }
    }
}
