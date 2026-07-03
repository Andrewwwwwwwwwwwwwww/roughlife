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
 * Giant dragonfly. Part layout ported from BetterEnd's dragonfly model
 * (Team BetterX, MIT — see THIRD-PARTY-NOTICES.md) so its 64x64 texture maps
 * exactly; the animation code is original.
 */
public class RazorwingModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftWingBase;
    private final ModelPart rightWingBase;

    public RazorwingModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.tail = body.getChild("tail");
        this.leftWing = body.getChild("left_wing");
        this.rightWing = body.getChild("right_wing");
        this.leftWingBase = body.getChild("left_wing_base");
        this.rightWingBase = body.getChild("right_wing_base");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.0f, -4.0f, 0.0f, 4.0f, 4.0f, 9.0f),
                PartPose.offset(2.0f, 21.5f, -4.0f));
        body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(17, 0)
                        .addBox(-1.5f, -1.5f, -2.5f, 3.0f, 3.0f, 3.0f),
                PartPose.offsetAndRotation(-2.0f, -2.0f, 0.0f, 0.3491f, 0.0f, 0.0f));
        PartDefinition tail = body.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(26, 0)
                        .addBox(-1.5f, -1.5f, 0.0f, 3.0f, 3.0f, 7.0f),
                PartPose.offset(-2.0f, -2.0f, 9.0f));
        tail.addOrReplaceChild("tail_fin",
                CubeListBuilder.create().texOffs(36, 0)
                        .addBox(-1.0f, -1.0f, 0.0f, 2.0f, 2.0f, 10.0f),
                PartPose.offset(0.0f, 0.0f, 7.0f));
        body.addOrReplaceChild("left_wing",
                CubeListBuilder.create().texOffs(0, 13)
                        .addBox(-15.0f, 0.0f, -3.0f, 15.0f, 0.0f, 4.0f),
                PartPose.offset(-2.0f, -4.0f, 4.0f));
        body.addOrReplaceChild("right_wing",
                CubeListBuilder.create().texOffs(0, 13).mirror()
                        .addBox(0.0f, 0.0f, -3.0f, 15.0f, 0.0f, 4.0f),
                PartPose.offset(-2.0f, -4.0f, 4.0f));
        body.addOrReplaceChild("left_wing_base",
                CubeListBuilder.create().texOffs(4, 17)
                        .addBox(-12.0f, 0.0f, -2.5f, 12.0f, 0.0f, 3.0f),
                PartPose.offset(-2.0f, -4.0f, 8.0f));
        body.addOrReplaceChild("right_wing_base",
                CubeListBuilder.create().texOffs(4, 17).mirror()
                        .addBox(0.0f, 0.0f, -2.5f, 12.0f, 0.0f, 3.0f),
                PartPose.offset(-2.0f, -4.0f, 8.0f));
        body.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(50, 1)
                        .addBox(0.0f, 0.0f, 0.0f, 0.0f, 3.0f, 6.0f),
                PartPose.offsetAndRotation(-1.0f, 0.0f, 1.0f, 0.0f, 0.0f, -0.5236f));
        body.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(50, 1)
                        .addBox(0.0f, 0.0f, 0.0f, 0.0f, 3.0f, 6.0f),
                PartPose.offsetAndRotation(-3.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.5236f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        // Frantic wing buzz, gentle tail sway, slight hover bob.
        float flap = Mth.sin(state.ageInTicks * 2.2f) * 0.55f;
        this.leftWing.zRot = 0.25f + flap;
        this.rightWing.zRot = -0.25f - flap;
        this.leftWingBase.zRot = 0.15f + flap * 0.7f;
        this.rightWingBase.zRot = -0.15f - flap * 0.7f;
        this.tail.yRot = Mth.sin(state.ageInTicks * 0.25f) * 0.12f;
        this.body.y = 21.5f + Mth.sin(state.ageInTicks * 0.35f) * 0.6f;
        this.head.xRot = 0.3491f + Mth.sin(state.ageInTicks * 0.2f) * 0.08f;
    }
}
