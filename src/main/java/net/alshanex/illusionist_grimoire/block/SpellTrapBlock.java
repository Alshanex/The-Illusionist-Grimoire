package net.alshanex.illusionist_grimoire.block;

import com.mojang.serialization.MapCodec;
import net.alshanex.illusionist_grimoire.registry.IGBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SpellTrapBlock extends BaseEntityBlock {
    public static final MapCodec<SpellTrapBlock> CODEC = simpleCodec(SpellTrapBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final double THICKNESS = 0.0625;

    // VoxelShapes for each direction (thin plate attached to face)
    private static final VoxelShape DOWN_SHAPE = Block.box(0, 0, 0, 16, THICKNESS * 16, 16);
    private static final VoxelShape UP_SHAPE = Block.box(0, 16 - (THICKNESS * 16), 0, 16, 16, 16);
    private static final VoxelShape NORTH_SHAPE = Block.box(0, 0, 0, 16, 16, THICKNESS * 16);
    private static final VoxelShape SOUTH_SHAPE = Block.box(0, 0, 16 - (THICKNESS * 16), 16, 16, 16);
    private static final VoxelShape WEST_SHAPE = Block.box(0, 0, 0, THICKNESS * 16, 16, 16);
    private static final VoxelShape EAST_SHAPE = Block.box(16 - (THICKNESS * 16), 0, 0, 16, 16, 16);

    public SpellTrapBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public SpellTrapBlock() {
        this(Properties.of()
                .strength(0.2F)
                .sound(SoundType.STONE)
                .noCollission()
                .noOcclusion()
                .lightLevel(state -> 3)
                .pushReaction(PushReaction.DESTROY));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN -> DOWN_SHAPE;
            case UP -> UP_SHAPE;
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getClickedFace();
        BlockPos blockPos = context.getClickedPos();
        Level level = context.getLevel();

        // Check if we can attach to the clicked face
        BlockPos attachPos = blockPos.relative(direction.getOpposite());
        BlockState attachState = level.getBlockState(attachPos);

        if (canAttachTo(level, direction, attachPos, attachState)) {
            return this.defaultBlockState().setValue(FACING, direction);
        }

        // Try other faces if the clicked face doesn't work
        for (Direction dir : context.getNearestLookingDirections()) {
            BlockPos testPos = blockPos.relative(dir.getOpposite());
            BlockState testState = level.getBlockState(testPos);
            if (canAttachTo(level, dir, testPos, testState)) {
                return this.defaultBlockState().setValue(FACING, dir);
            }
        }

        return null;
    }

    private boolean canAttachTo(BlockGetter level, Direction direction, BlockPos pos, BlockState state) {
        return Block.isFaceFull(state.getBlockSupportShape(level, pos), direction);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos attachedPos = pos.relative(facing.getOpposite());
        return level.getBlockState(attachedPos).isFaceSturdy(level, attachedPos, facing);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (direction.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpellTrapBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type,
                IGBlockEntityRegistry.SPELL_TRAP.get(),
                SpellTrapBlockEntity::serverTick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SpellTrapBlockEntity) {
                ((SpellTrapBlockEntity) blockEntity).drops();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
