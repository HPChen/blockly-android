/*
 *  Copyright  2015 Google Inc. All Rights Reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.google.blockly.ui;

import android.test.AndroidTestCase;

import com.google.blockly.MockBlocksProvider;
import com.google.blockly.control.ConnectionManager;
import com.google.blockly.model.Block;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the {@link WorkspaceHelper}.
 */
public class WorkspaceHelperTest extends AndroidTestCase {
    private WorkspaceHelper mWorkspaceHelper;
    private WorkspaceView mWorkspaceView;

    @Mock
    ConnectionManager mockConnectionManager;

    @Override
    public void setUp() {
        // To solve some issue with Dexmaker.  This allows us to use mockito.
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getPath());
        MockitoAnnotations.initMocks(this);

        mWorkspaceView = new WorkspaceView(getContext());
        mWorkspaceHelper = new WorkspaceHelper(mWorkspaceView, null);
    }

    // test getNearestParentBlockGroup
    public void testGetNearestParentBlockGroup() {
        List<Block> blocks = new ArrayList<>();
        Block root = MockBlocksProvider.makeStatementBlock();
        Block cur = root;
        // Make a chain of statement blocks, all of which will be in the same block group.
        for (int i = 0; i < 3; i++) {
            cur.getNextConnection().connect(
                    MockBlocksProvider.makeStatementBlock().getPreviousConnection());
            cur = cur.getNextBlock();
        }

        // Add a block that has inputs at the end of the chain.
        cur.getNextConnection().connect(MockBlocksProvider.makeDummyBlock().getPreviousConnection());
        cur = cur.getNextBlock();

        // Connect a block as an input.  It should be in its own block group.
        Block hasOutput = MockBlocksProvider.makeSimpleValueBlock();
        cur.getInputByName("input2").getConnection().connect(hasOutput.getOutputConnection());

        blocks.add(root);

        // Add a completely unconnected block.
        blocks.add(MockBlocksProvider.makeStatementBlock());

        createViews(blocks);

        assertEquals(mWorkspaceHelper.getNearestParentBlockGroup(root),
                mWorkspaceHelper.getNearestParentBlockGroup(cur));

        assertNotSame(mWorkspaceHelper.getNearestParentBlockGroup(blocks.get(0)),
                mWorkspaceHelper.getNearestParentBlockGroup(blocks.get(1)));

        assertNotSame(mWorkspaceHelper.getNearestParentBlockGroup(root),
                mWorkspaceHelper.getNearestParentBlockGroup(hasOutput));
    }


    // test getRootBlockGroup
    public void testGetRootBlockGroup() {
        List<Block> blocks = new ArrayList<>();
        Block root = MockBlocksProvider.makeDummyBlock();
        Block cur = root;
        // Make a chain of blocks with statement inputs.  Each block will be connected to a
        // statement input on the block above.
        for (int i = 0; i < 3; i++) {
            cur.getInputByName("input6").getConnection().connect(
                    MockBlocksProvider.makeDummyBlock().getPreviousConnection());
            cur = cur.getInputByName("input6").getConnection().getTargetBlock();
        }
        // At the end of the chain, add a block as a "next".  It will still be in the same root
        // block group.
        Block finalBlock = MockBlocksProvider.makeStatementBlock();
        cur.getNextConnection().connect(finalBlock.getPreviousConnection());
        blocks.add(root);

        // Add a completely unconnected block.
        blocks.add(MockBlocksProvider.makeDummyBlock());

        createViews(blocks);

        assertEquals(mWorkspaceHelper.getRootBlockGroup(root),
                mWorkspaceHelper.getRootBlockGroup(cur));

        assertEquals(mWorkspaceHelper.getRootBlockGroup(root),
                mWorkspaceHelper.getRootBlockGroup(finalBlock));

        assertNotSame(mWorkspaceHelper.getRootBlockGroup(blocks.get(0)),
                mWorkspaceHelper.getRootBlockGroup(blocks.get(1)));
    }

    private void createViews(List<Block> blocks) {
        // Create views for all of the blocks we're interested in.
        for (int i = 0; i < blocks.size(); i++) {
            BlockGroup bg = new BlockGroup(getContext(), mWorkspaceHelper);
            mWorkspaceHelper.obtainBlockView(blocks.get(i), bg, mockConnectionManager);
            mWorkspaceView.addView(bg);
        }
    }

}
