package site.siredvin.gttruesteam.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BeatingBoilerHuskBlock extends Block {

    private static final Direction[] HORIZONTAL_DIRECTIONS = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    };
    private static final int BEAT_LENGTH = 40;

    public BeatingBoilerHuskBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() > 0.42F) {
            return;
        }

        long phase = Math.floorMod(level.getGameTime() + pos.asLong(), BEAT_LENGTH);
        boolean inhaling = phase >= 24;
        Direction direction = HORIZONTAL_DIRECTIONS[random.nextInt(HORIZONTAL_DIRECTIONS.length)];

        double ventOffset = (random.nextInt(3) - 1) * 0.18D;
        double height = 0.46D + random.nextDouble() * 0.2D;
        double sideOffset = inhaling ? 0.72D : 0.52D;

        double x = pos.getX() + 0.5D + direction.getStepX() * sideOffset;
        double y = pos.getY() + height;
        double z = pos.getZ() + 0.5D + direction.getStepZ() * sideOffset;

        if (direction.getAxis() == Direction.Axis.Z) {
            x += ventOffset;
        } else {
            z += ventOffset;
        }

        double outwardSpeed = 0.012D + random.nextDouble() * 0.02D;
        double inwardSpeed = 0.026D + random.nextDouble() * 0.016D;
        double speed = inhaling ? -inwardSpeed : outwardSpeed;
        double sideJitter = (random.nextDouble() - 0.5D) * 0.01D;

        double xSpeed = direction.getStepX() * speed;
        double ySpeed = inhaling ? -0.006D + random.nextDouble() * 0.008D : 0.012D + random.nextDouble() * 0.02D;
        double zSpeed = direction.getStepZ() * speed;

        if (direction.getAxis() == Direction.Axis.Z) {
            xSpeed += sideJitter;
        } else {
            zSpeed += sideJitter;
        }

        level.addParticle(ParticleTypes.CLOUD, x, y, z, xSpeed, ySpeed, zSpeed);

        if (!inhaling && phase > 8 && phase < 20 && random.nextFloat() < 0.28F) {
            level.addParticle(
                    ParticleTypes.CLOUD,
                    x + (random.nextDouble() - 0.5D) * 0.08D,
                    y + 0.08D + random.nextDouble() * 0.05D,
                    z + (random.nextDouble() - 0.5D) * 0.08D,
                    xSpeed * 0.55D,
                    ySpeed * 0.75D,
                    zSpeed * 0.55D);
        }
    }
}
