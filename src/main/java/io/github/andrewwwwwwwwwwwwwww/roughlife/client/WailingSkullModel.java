package io.github.andrewwwwwwwwwwwwwww.roughlife.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

/**
 * A single 8x8x8 cube using the standard head-cross UV layout on a 32x16
 * texture, with a slow hover bob.
 */
public class WailingSkullModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart skull;

    public WailingSkullModel(ModelPart root) {
        super(root);
        this.skull = root.getChild("skull");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("skull",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f),
                PartPose.offset(0.0f, 24.0f, 0.0f));
        return LayerDefinition.create(mesh, 32, 16);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        // Hover bob only — the renderer already applies body yaw to the whole
        // model, so rotating the part here would double-rotate it.
        this.skull.y = 24.0f + Mth.sin(state.ageInTicks * 0.12f) * 0.8f;
    }
}
