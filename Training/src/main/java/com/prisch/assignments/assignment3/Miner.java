package com.prisch.assignments.assignment3;

import com.prisch.assignments.Settings;
import com.prisch.assignments.assignment4.Coinbase;
import com.prisch.assignments.assignment6.TransactionRepository;
import com.prisch.ignore.StompSessionHolder;
import com.prisch.ignore.blocks.BlockRepository;
import com.prisch.ignore.messages.LocalMessages;
import com.prisch.ignore.mining.MiningController;
import com.prisch.reference.blockchain.BlockchainProperties;
import com.prisch.reference.blocks.Block;
import com.prisch.reference.services.HashService;
import com.prisch.reference.services.KeyService;
import com.prisch.reference.transactions.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Miner implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Miner.class);

    private volatile boolean interrupted;
    private volatile boolean foundBlock;

    private volatile long nonce = 0;
    private volatile String previousBlockHash;

    @Autowired private MiningController miningController;
    @Autowired private BlockRepository blockRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private KeyService keyService;
    @Autowired private HashService hashService;
    @Autowired private BlockchainProperties blockchainProperties;
    @Autowired private StompSessionHolder stompSessionHolder;
    @Autowired private LocalMessages localMessages;

    public void interrupt() {
        interrupted = true;
    }

    public long getNonce() {
        return nonce;
    }

    public String getPreviousBlockHash() {
        return previousBlockHash;
    }

    @Override
    public void run() {
        Block previousBlock = blockRepository.getLastBlock();
        previousBlockHash = previousBlock.getHash();

        List<Transaction> transactions = buildTransactions(previousBlock.getHeight() + 1);
        Block proposedBlock = buildBlock(previousBlock, transactions);

        mine(previousBlock, transactions, proposedBlock);
    }

    private void mine(Block previousBlock, List<Transaction> transactions, Block proposedBlock) {
        // TODO: [3B]
        // The current mining strategy will only ever reward the fastest computer.
        // Change this strategy (the way the nonce values are chosen) to give yourself a chance at mining a block.

        // TODO: [3+]
        // Team up with other developers and create a mining pool

        while (!interrupted) {
            String blockHash = hash(transactions, previousBlock.getHash(), nonce);

            if (satifiesHashCheck(blockHash)) {
                proposedBlock.setNonce(nonce);
                proposedBlock.setHash(hashService.trunc(blockHash));

                foundBlock = true;
                miningController.completeMining();

                LOG.info(proposedBlock.toJson());
                stompSessionHolder.getStompSession().send("/app/postBlock", proposedBlock);
            }

            ++nonce;
        }

        if (!foundBlock) {
            localMessages.addMessage(String.format("Mining was interrupted before a new block could found (previous block hash: %s)", previousBlockHash));
        }
    }

    private List<Transaction> buildTransactions(int proposedHeight) {
        List<Transaction> transactions = transactionRepository.getMostProfitableTransactions(blockchainProperties.getTransactionLimit() - 1); // Consider the Coinbase Transaction
        int transactionFees = transactions.stream().mapToInt(Transaction::getFeeAmount).sum();

        Coinbase coinbase = Coinbase.with(proposedHeight, transactionFees, keyService, hashService);
        if (coinbase != null) {
            transactions.add(0, coinbase);
        }

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
        return hashService.hashWithoutTrunc(serializedTransaction);
    }

    private boolean satifiesHashCheck(String blockHash) {
        // TODO: [3A]
        // Determine whether the provided blockHash satisfies the proof of work check by comparing with {@link BlockchainProperties.getHashCheck()}
        // This check should be successful if the starting substring is smaller than or equal to the hashCheck
        // In other words, if the hashCheck is 3 characters long then the first 3 characters of the blockHash should compareTo < 0

        return false;
    }
}
