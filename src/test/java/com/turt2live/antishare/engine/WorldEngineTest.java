package com.turt2live.antishare.engine;

import com.turt2live.antishare.engine.defaults.DefaultBlockTypeList;
import com.turt2live.antishare.io.BlockManager;
import com.turt2live.antishare.io.memory.MemoryBlockManager;
import com.turt2live.antishare.utils.ASGameMode;
import com.turt2live.antishare.utils.ASLocation;
import com.turt2live.antishare.utils.BlockType;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WorldEngineTest {

    @Test
    public void aTestInstance1() {
        WorldEngine engine = new WorldEngine("test");
        assertEquals("test", engine.getWorldName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void bTestInstance2(){
        new WorldEngine(null);
    }

    @Test
    public void cTestBlockManager() {
        WorldEngine engine = new WorldEngine("test");
        assertTrue(engine.getBlockManager() instanceof MemoryBlockManager);

        BlockManager manager = mock(BlockManager.class);
        engine.setBlockManager(manager);
        assertEquals(manager, engine.getBlockManager());
    }

    @Test(expected = IllegalArgumentException.class)
    public void dTestBlockManagerNull() {
        WorldEngine engine = new WorldEngine("test");
        engine.setBlockManager(null);
    }

    @Test
    public void eTestBlockList() {
        WorldEngine engine = new WorldEngine("test");
        for (ASGameMode gameMode : ASGameMode.values()) {
            assertTrue(engine.getTrackedBlocks(gameMode) instanceof DefaultBlockTypeList);
        }

        for (ASGameMode gameMode : ASGameMode.values()) {
            BlockTypeList list = mock(BlockTypeList.class);
            engine.setTrackedBlocks(gameMode, list);
            assertEquals(list, engine.getTrackedBlocks(gameMode));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void fTestBlockListNull1() {
        WorldEngine engine = new WorldEngine("test");
        engine.setTrackedBlocks(null, mock(BlockTypeList.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void gTestBlockListNull2() {
        WorldEngine engine = new WorldEngine("test");
        engine.setTrackedBlocks(ASGameMode.ADVENTURE, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void hTestBlockListNull3() {
        WorldEngine engine = new WorldEngine("test");
        engine.setTrackedBlocks(null, null);
    }

    @Test
    public void iTestProcess() {
        ASLocation location = new ASLocation(0, 90, 0);
        BlockTypeList list = mock(BlockTypeList.class);
        BlockManager manager = mock(BlockManager.class);
        WorldEngine engine = new WorldEngine("test");

        when(list.isTracked(location)).thenReturn(true);

        engine.processBlockPlace(new ASLocation(90, 90, 90), BlockType.ADVENTURE);
        verifyZeroInteractions(manager);

        engine.processBlockPlace(location, BlockType.ADVENTURE);
        verify(manager).setBlockType(any(ASLocation.class), any(BlockType.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void jTestProcessNull1() {
        WorldEngine engine = new WorldEngine("test");
        engine.processBlockPlace(null, BlockType.ADVENTURE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void kTestProcessNull2() {
        WorldEngine engine = new WorldEngine("test");
        engine.processBlockPlace(new ASLocation(0, 0, 0), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void lTestProcessNull3() {
        WorldEngine engine = new WorldEngine("test");
        engine.processBlockPlace(null, null);
    }

}
