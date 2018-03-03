package blockchain_pkg;

//Block Chain should maintain only limited block nodes to satisfy the functions
//You should not have all the blocks added to the block chain in memory 
//as it would cause a memory overflow.
import java.util.Arrays;

import java.util.ArrayList;

public class BlockChain {
	public static final int CUT_OFF_AGE = 10;
	public BlockNode rootNode;
	public TransactionPool txPool; 
	public UTXOPool utxoPool;

	private class BlockNode {
		public int blockHeight;
		public Block b;
		public BlockNode parent;
		public ArrayList<BlockNode> children;
		
		public BlockNode (Block b) {
			this.b = b;
			this.parent = null;
			this.blockHeight =0;
			this.children = new ArrayList<>();
		}
		
		public void addChild(Block b) {
			BlockNode child = new BlockNode(b);
			child.setParent(this);
			child.blockHeight = this.blockHeight+1;
			this.children.add(child);
		}
		
		public ArrayList<BlockNode> getChildren () {
			return children;
		}
		
		private void setParent(BlockNode parent) {
			this.parent = parent;
		}
		

	}
	
	// Find a BlockNode with a given hash (recursive)
	public BlockNode getNodeWithHash(BlockNode bNode,byte[] hash) {
		if (Arrays.equals(bNode.b.getHash(), hash)) {
			System.out.println("Found block with prevHash at Height= " + bNode.blockHeight);
			return bNode;
		} else {
			//bNode.getChildren().forEach(each ->  getNodeWithHash(each, hash));
			//System.out.println("prevHash = "+ Arrays.toString(hash));
			//System.out.println("currHash = "+ Arrays.toString(bNode.b.getHash()));
			for (BlockNode each: bNode.getChildren() ) {
				return getNodeWithHash(each, hash);
			}
			
			return null;
		}
	}	
	
	// Find a BlockNode with maxHeight (recursive)
	public BlockNode getNodeWithMaxHeight(BlockNode bNode) {
		if (bNode.getChildren().isEmpty()) {
			//System.out.println("Found block with maxHeight= " + bNode.blockHeight);
			return bNode;
		} else {
			//bNode.getChildren().forEach(each ->  getNodeWithHash(each, hash));
			//System.out.println("prevHash = "+ Arrays.toString(hash));
			//System.out.println(" = "+ Arrays.toString(bNode.b.getHash()));
			int maxHeight =-1;
			BlockNode maxHeightBlockNode = null;
			for (BlockNode each: bNode.getChildren() ) {
				BlockNode bN = getNodeWithMaxHeight(each);
				if (bN.blockHeight > maxHeight) {
					maxHeightBlockNode = bN;
					maxHeight = bN.blockHeight;
				}
				
			}
			return maxHeightBlockNode;
		}
	}		
	

	// Add all TX outputs and Coinbase to the utxo pool starting from this block to genesis block
	private void addUTXO(UTXOPool utxoPool, BlockNode blockNode) {
		ArrayList<Transaction> txs = blockNode.b.getTransactions();
		// Get all transaction outputs
		for (Transaction tx:txs) {	
			for (int i=0; i <tx.numOutputs(); i++) {
				UTXO utxo = new UTXO(tx.getHash(),i);
				utxoPool.addUTXO(utxo, tx.getOutput(i));
			}
		}
		// Get coinbase transaction
		UTXO utxo = new UTXO(blockNode.b.getCoinbase().getHash(),0);
		utxoPool.addUTXO(utxo, blockNode.b.getCoinbase().getOutput(0));
		
		// Recursive call to the parent block
        if (blockNode.parent != null) {
        	addUTXO(utxoPool, blockNode.parent);
        }

	}
	
	
	// Remove all TX inputs from the utxo pool starting from this block to genesis block
	private void removeUTXO(UTXOPool utxoPool, BlockNode blockNode) {
		ArrayList<Transaction> txs = blockNode.b.getTransactions();
		// Get all transaction inputs
		for (Transaction tx:txs) {	
			for (int i=0; i <tx.numInputs(); i++) {
				UTXO utxo = new UTXO(tx.getInput(i).prevTxHash,tx.getInput(i).outputIndex);
				utxoPool.removeUTXO(utxo);
			}
		}
		
		// Recursive call to the parent block
        if (blockNode.parent != null) {
        	removeUTXO(utxoPool, blockNode.parent);
        }

	}
	
	/**
	 * create an empty block chain with just a genesis block. Assume
	 * {@code genesisBlock} is a valid block
	 */
	public BlockChain(Block genesisBlock) {
		// IMPLEMENT THIS
		rootNode = new BlockNode(genesisBlock);
		txPool   = new TransactionPool();
	}

	/** Get the maximum height block */
	public Block getMaxHeightBlock() {
		// IMPLEMENT THIS
		BlockNode maxHeightBlockNode = getNodeWithMaxHeight(rootNode);
		return maxHeightBlockNode.b;
	}

	/** Get the UTXOPool for mining a new block on top of max height block */
	public UTXOPool getMaxHeightUTXOPool() {
		// IMPLEMENT THIS
		utxoPool = new UTXOPool();
		BlockNode maxHeightBlockNode = getNodeWithMaxHeight(rootNode);
		addUTXO(utxoPool, maxHeightBlockNode);
		removeUTXO(utxoPool, maxHeightBlockNode);
		return utxoPool;
	}

	/** Get the transaction pool to mine a new block */
	public TransactionPool getTransactionPool() {
		// IMPLEMENT THIS
		return txPool;
	}

	/**
	 * Add {@code block} to the block chain if it is valid. For validity, all
	 * transactions should be valid and block should be at
	 * {@code height > (maxHeight - CUT_OFF_AGE)}.
	 * 
	 * <p>
	 * For example, you can try creating a new block over the genesis block (block
	 * height 2) if the block chain height is {@code <=
	 * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot
	 * create a new block at height 2.
	 * 
	 * @return true if block is successfully added
	 */
	public boolean addBlock(Block block) {
		// IMPLEMENT THIS -PENDING
		System.out.println("addBlock start " );
		BlockNode leafNode = getNodeWithHash(rootNode,block.getPrevBlockHash());
		
		// Check if prevHash is in the current BlockChain
		if (leafNode== null) {
			System.out.println("PrevBlockHash is not found in Blockchain  prevBlockHash= " + Arrays.toString(block.getPrevBlockHash()));
			return false;
		}
		
		// Check if all transaction inputs are in the UTXO for this branch of the leafNode
		utxoPool = new UTXOPool();
		addUTXO(utxoPool, leafNode);
		removeUTXO(utxoPool, leafNode);
		ArrayList<Transaction> txs = block.getTransactions();
		// Get all transaction inputs
		for (Transaction tx:txs) {	
			for (int i=0; i <tx.numInputs(); i++) {
				UTXO utxo = new UTXO(tx.getInput(i).prevTxHash,tx.getInput(i).outputIndex);
				if (utxoPool.contains(utxo) == false) {
					return false;
				}
			}	
		}
		
		// Check for CUT_OFF_AGE
		BlockNode MaxNode = getNodeWithMaxHeight(rootNode);
		System.out.println("MaxHeight= " + MaxNode.blockHeight );
		if ((MaxNode.blockHeight - leafNode.blockHeight) > CUT_OFF_AGE) {
			return false;
		}
		
		// Add the block
		leafNode.addChild(block);
		System.out.println("addBlock done " );
		
		// Update Transaction Pool
		txs = block.getTransactions();
		for (Transaction tx:txs) {	
			txPool.removeTransaction(tx.getHash());
		}
		
		return true;
	}

	/** Add a transaction to the transaction pool */
	public void addTransaction(Transaction tx) {
		// IMPLEMENT THIS
		txPool.addTransaction(tx);
	}
}