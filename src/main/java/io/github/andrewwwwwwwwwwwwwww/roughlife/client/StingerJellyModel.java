package io.github.andrewwwwwwwwwwwwwww.roughlife.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Box jellyfish: small core, big inverted bell, four flat tentacle planes at
 * 90-degree intervals. Part layout derived from BetterEnd's cubozoa model
 * (Team BetterX, MIT — see THIRD-PARTY-NOTICES.md) so its 48x48 textures map
 * exactly; the pulse/sway animation code is original.
 */
public class StingerJellyModel extends EntityModel<JellyRenderState> {
    private final ModelPart body;
    private final ModelPart[] tentacles = new ModelPart[4];

    public StingerJellyModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        for (int i = 0; i < 4; i++) {
            this.tentacles[i] = body.getChild("tentacle_" + i);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 17)
                        .addBox(-2.0f, -12.5f, -2.0f, 4.0f, 4.0f, 4.0f),
                PartPose.offset(0.0f, 24.0f, 0.0f));
        body.addOrReplaceChild("bell",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-5.0f, -7.0f, -5.0f, 10.0f, 7.0f, 10.0f),
                PartPose.offsetAndRotation(0.0f, -14.0f, 0.0f, 0.0f, 0.0f, -3.1416f));
        for (int i = 0; i < 4; i++) {
            float angle = ((float) Math.PI / 2.0f) * i;
            body.addOrReplaceChild("tentacle_" + i,
                    CubeListBuilder.create().texOffs(16, 17)
                            .addBox(-4.0f, 0.0f, 0.0f, 8.0f, 7.0f, 0.0f),
                    PartPose.offsetAndRotation(
                            Mth.sin(angle) * 4.5f, -7.0f, Mth.cos(angle) * 4.5f,
                            0.0f, angle, 0.0f));
        }
        return LayerDefinition.create(mesh, 48, 48);
    }

    @Override
    public void setupAnim(JellyRenderState state) {
        super.setupAnim(state);
        // Slow medusa pulse and trailing tentacle sway.
        float pulse = Mth.sin(state.ageInTicks * 0.13f);
        this.body.y = 24.0f + pulse * 1.2f;
        for (int i = 0; i < 4; i++) {
            this.tentacles[i].xRot = 0.12f + Mth.sin(state.ageInTicks * 0.13f + i * 1.6f) * 0.18f;
        }
    }
}
