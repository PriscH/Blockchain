package com.prisch.mining;

import com.prisch.blockchain.BlockchainProperties;
import com.prisch.StompSessionHolder;
import com.prisch.blocks.Block;
import com.prisch.blocks.BlockRepository;
import com.prisch.global.Constants;
import com.prisch.global.Settings;
import com.prisch.services.HashService;
import com.prisch.services.KeyService;
import com.prisch.transactions.Coinbase;
import com.prisch.transactions.Transaction;
import com.prisch.transactions.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Miner implements Runnable {

    private volatile boolean interrupted;
    private volatile long nonce = 0;

    @Autowired private MiningController miningController;
    @Autowired private BlockRepository blockRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private KeyService keyService;
    @Autowired private HashService hashService;
    @Autowired private BlockchainProperties blockchainProperties;
    @Autowired private StompSessionHolder stompSessionHolder;

    public void interrupt() {
        interrupted = true;
    }

    public long getNonce() {
        return nonce;
    }

    @Override
    public void run() {
        Block previousBlock = blockRepository.getTopBlock();
        List<Transaction> transactions = buildTransactions();
        Block proposedBlock = buildBlock(previousBlock, transactions);

        mine(previousBlock, transactions, proposedBlock);
    }

    private void mine(Block previousBlock, List<Transaction> transactions, Block proposedBlock) {
        while (!interrupted) {
            String blockHash = hash(transactions, previousBlock.getHash(), nonce);

            if (satifiesHashCheck(blockHash)) {
                proposedBlock.setNonce(nonce);
                proposedBlock.setHash(hashService.trunc(blockHash));

                miningController.completeMining();
                stompSessionHolder.getStompSession().send("/app/postBlock", proposedBlock);
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
        int checkLength = blockchainProperties.getHashCheck().length();
        String blockHashStart = blockHash.substring(0, checkLength);

        return (blockHashStart.compareTo(blockchainProperties.getHashCheck()) <= 0);
    }
}
