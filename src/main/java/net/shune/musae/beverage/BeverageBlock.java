package net.shune.musae.beverage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class BeverageBlock extends HorizontalDirectionalBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape SHAPE_BOTTLE_NORTH = Shapes.box(
            5.0 / 16.0, 0.0, 5.0 / 16.0,
            11.0 / 16.0, 12.0 / 16.0, 11.0 / 16.0);

    private static final VoxelShape SHAPE_GLASS_NORTH = Shapes.box(
            10.0 / 16.0, 0.0, 5.0 / 16.0,
            15.0 / 16.0, 6.0 / 16.0, 11.0 / 16.0);

    private static final Map<Direction, VoxelShape> BOTTLE_SHAPES = buildRotatedShapes(SHAPE_BOTTLE_NORTH);
    private static final Map<Direction, VoxelShape> GLASS_SHAPES = buildRotatedShapes(SHAPE_GLASS_NORTH);

    public enum Profile {
        BOTTLE(BOTTLE_SHAPES),
        GLASS(GLASS_SHAPES);

        final Map<Direction, VoxelShape> shapes;
        Profile(Map<Direction, VoxelShape> shapes) { this.shapes = shapes; }
    }

    private final Profile profile;
    private final Supplier<? extends Item> dropItem;

    public BeverageBlock(Profile profile, Supplier<? extends Item> dropItem) {
        super(BlockBehaviour.Properties.of()
                .strength(0.3f, 0.3f)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .pushReaction(PushReaction.DESTROY)
                .instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.HAT));
        this.profile = profile;
        this.dropItem = dropItem;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return profile.shapes.get(state.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return profile.shapes.get(state.getValue(FACING));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(dropItem.get()));
        return drops;
    }

    private static Map<Direction, VoxelShape> buildRotatedShapes(VoxelShape baseShape) {
        VoxelShape northShape = rotateShapeY(baseShape, Rotation.COUNTERCLOCKWISE_90);
        Map<Direction, VoxelShape> map = new EnumMap<>(Direction.class);
        map.put(Direction.NORTH, northShape);
        map.put(Direction.EAST, rotateShapeY(northShape, Rotation.CLOCKWISE_90));
        map.put(Direction.SOUTH, rotateShapeY(northShape, Rotation.CLOCKWISE_180));
        map.put(Direction.WEST, rotateShapeY(northShape, Rotation.COUNTERCLOCKWISE_90));
        return map;
    }

    private static VoxelShape rotateShapeY(VoxelShape shape, Rotation rotation) {
        VoxelShape[] result = { Shapes.empty() };
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double x1 = minX - 0.5;
            double z1 = minZ - 0.5;
            double x2 = maxX - 0.5;
            double z2 = maxZ - 0.5;

            double nx1, nz1, nx2, nz2;
            switch (rotation) {
                case CLOCKWISE_90 -> {
                    nx1 = -z2; nz1 = x1;
                    nx2 = -z1; nz2 = x2;
                }
                case CLOCKWISE_180 -> {
                    nx1 = -x2; nz1 = -z2;
                    nx2 = -x1; nz2 = -z1;
                }
                case COUNTERCLOCKWISE_90 -> {
                    nx1 = z1; nz1 = -x2;
                    nx2 = z2; nz2 = -x1;
                }
                default -> {
                    nx1 = x1; nz1 = z1;
                    nx2 = x2; nz2 = z2;
                }
            }

            VoxelShape box = Shapes.box(
                    nx1 + 0.5, minY, nz1 + 0.5,
                    nx2 + 0.5, maxY, nz2 + 0.5);
            result[0] = Shapes.join(result[0], box, BooleanOp.OR);
        });
        return result[0].optimize();
    }
}