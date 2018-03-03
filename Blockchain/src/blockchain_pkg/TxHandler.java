package blockchain_pkg;

import java.util.ArrayList;
import java.util.Arrays;

//import scrooge_pkg.Transaction.Output;

public class TxHandler {
	private UTXOPool localUtxoPool;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
    	localUtxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
    	ArrayList<Transaction.Input> allInputs = tx.getInputs();
    	int   inputIndex                       = 0;
    	
        // (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
        for (Transaction.Input in : allInputs) {
        	UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
        	System.out.println("\nValidating Transaction Input #" + inputIndex + " with UTXO ...");
        	System.out.println("Prev Output Hash=" + Arrays.toString(in.prevTxHash) + "   Prev Output Index=" + in.outputIndex );
        	if (localUtxoPool.contains(utxo) == false) {
            	System.out.println("FAIL:Transaction is not inside UTXOPool ");
            	return false;        		
        	} else {
        		System.out.println("PASS:Transaction is inside UTXOPool ");
        	}
        	inputIndex++;
        }
        
        // (2) the signatures on each input of {@code tx} are valid, 
        allInputs        = tx.getInputs();
        inputIndex       = 0;
        for (Transaction.Input in : allInputs) {
        	Transaction.Output out;
        	UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
        	out = localUtxoPool.getTxOutput(utxo);
        	System.out.println("\nValidating Input signature #" + inputIndex);
        	System.out.println("Prev Output Hash=" + Arrays.toString(in.prevTxHash) + "   Prev Output Index=" + in.outputIndex );
        	System.out.println("\nout.address=" + out.address + "\ntx.getRawDataToSign(inputIndex) " + Arrays.toString(tx.getRawDataToSign(inputIndex)) + "\nin.signature=" + Arrays.toString(in.signature)); 
        	if (Crypto.verifySignature(out.address, tx.getRawDataToSign(inputIndex), in.signature) == false) {
        		System.out.println("FAIL:Transaction Input Signature is not valid ");
        		return false;
        	} else {
        		System.out.println("PASS:Transaction Input Signature is valid");
        	}
        	inputIndex++;
        }
        
        // (3) no UTXO is claimed multiple times by {@code tx},
        allInputs        = tx.getInputs();
        inputIndex       = 0;
        System.out.println("\nChecking for multiple use of UTXO...");
        System.out.println("allInputs.size() = " + allInputs.size());
        for (int i=0; i < allInputs.size(); i++) {
        	Transaction.Input in0 = allInputs.get(i);
        	for (int j=0;j<i ; j++) {
        		Transaction.Input in1 = allInputs.get(j);
        		if (Arrays.equals(in0.prevTxHash, in1.prevTxHash) & (in0.outputIndex == in1.outputIndex)) {
        			System.out.println("FAIL:Same UTXO used multiple time i=" + i + "  j=" +j);
        			return false;
        		} 
        	}
        	System.out.println("PASS:No duplicate for TX id=" +i);
        }        
        
        // (4) all of {@code tx}s output values are non-negative, and
    	ArrayList<Transaction.Output> allOutputs = tx.getOutputs();
    	System.out.println("\nChecking outputs for non-negative values...");
        for (int i=0; i < allOutputs.size(); i++) {   
        	Transaction.Output out = allOutputs.get(i);
        	if (out.value < 0) {
        		System.out.println("FAIL: Output value is negative index=" + i + " value=" +out.value);
        		return false;
        	}
        	System.out.println("PASS:No negative values for index=" + i);
        }
        
        
        // (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
        //     values; and false otherwise.   
        double sumOfInputs  =0;
        double sumOfOutputs =0;
        
        for (int i=0; i < allInputs.size(); i++) {
        	Transaction.Input in = allInputs.get(i);
        	UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
        	Transaction.Output out = localUtxoPool.getTxOutput(utxo);
        	sumOfInputs += out.value;
        }
        
        for (int i=0; i < allOutputs.size(); i++) {   
        	Transaction.Output out = allOutputs.get(i);
        	sumOfOutputs += out.value;
        }
        
        if (sumOfOutputs > sumOfInputs) {
        	System.out.println("FAIL: sumOfInputs=" + sumOfInputs + " sumOfOutputs=" +sumOfOutputs);
        } else {
        	System.out.println("PASS: sumOfInputs=" + sumOfInputs + " sumOfOutputs=" +sumOfOutputs);
        }
    	
    	return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
    	Transaction[] vettedTxs = new Transaction[possibleTxs.length];
    	int index =0;
        for (int i=0; i < possibleTxs.length; i++) {
        	Transaction tx = possibleTxs[i];
        	
        	if (isValidTx(tx)) {
        		vettedTxs[index] = tx;
        		index++;
            	ArrayList<Transaction.Input> allInputs = tx.getInputs();
            	
                for (int j=0; j < allInputs.size(); j++) {
                	Transaction.Input in = allInputs.get(j);
                	UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
                	localUtxoPool.removeUTXO(utxo);
                }
        		
        	}
        }
    	
    	return vettedTxs;
    }
    
    public UTXOPool getUTXOPool() {
    	
    	return localUtxoPool;
    }

}

