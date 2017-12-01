package com.prisch.mining;

import com.prisch.BlockchainProperties;
import com.prisch.blocks.Block;
import com.prisch.blocks.BlockRepository;
import com.prisch.global.Constants;
import com.prisch.global.Settings;
import com.prisch.services.HashService;
import com.prisch.services.KeyService;
import com.prisch.transactions.Coinbase;
import com.prisch.transactions.Transaction;
import com.prisch.transactions.TransactionRepository;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

public class Miner implements Runnable {

    private volatile boolean interrupted;

    private BlockRepository blockRepository;
    private TransactionRepository transactionRepository;
    private KeyService keyService;
    private HashService hashService;
    private BlockchainProperties blockchainProperties;

    public void interrupt() {
        interrupted = true;
    }

    @Override
    public void run() {
        Block previousBlock = blockRepository.getTopBlock();
        List<Transaction> transactions = buildTransactions();
        Block blockInProgress = buildBlock(previousBlock, transactions);

        long nonce = 0;
        while (!interrupted) {
            String blockHash = hash(transactions, previousBlock.getHash(), nonce);
            if (satifiesHashCheck(blockHash)) {
                blockInProgress.setNonce(nonce);
                blockInProgress.setHash(hashService.trunc(blockHash));
                interrupted = true;

                // TODO: Post the block
                // TODO: Restart mining once our block has been accepted
            }

            ++nonce;
        }
    }

    private List<Transaction> buildTransactions() {
        List<Transaction> transactions = transactionRepository.getMostProfitableTransactions(Constants.TRANSACTION_LIMIT - 1); // Consider the Coinbase Transaction
        int transactionFees = transactions.stream().mapToInt(Transaction::getFeeAmount).sum();

        Transaction.Output output = new Transaction.Output();
        output.setAmount(transactionFees + Constants.COINBASE_REWARD);
        try {
            output.setAddress(keyService.getAddress());
        } catch (NoSuchAlgorithmException | IOException ex) {
            throw new RuntimeException(ex);
        }

        Coinbase coinbase = new Coinbase(Settings.VERSION, output);
        transactions.add(coinbase);

        return transactions;
    }

    private Block buildBlock(Block previousBlock, List<Transaction> transactions) {
        Block block = new Block();

        block.setVersion(Settings.VERSION);
        block.setHeight(previousBlock.getHeight() + 1);

        block.setTransactions(transactions);

        block.setPreviousHash(previousBlock.getHash());
        block.setProperties(new HashMap<>());

        return block;
    }

    private String hash(List<Transaction> transactions, String previousHash, long nonce) {
        StringBuilder serializationBuilder = new StringBuilder();

        transactions.forEach(tx -> serializationBuilder.append(tx.getHash()));
        serializationBuilder.append(previousHash);
        serializationBuilder.append(nonce);

        String serializedTransaction = serializationBuilder.toString();

        try {
            return hashService.hashWithoutTrunc(serializedTransaction);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean satifiesHashCheck(String blockHash) {
        return blockHash.startsWith(blockchainProperties.getHashCheck());
    }
}
