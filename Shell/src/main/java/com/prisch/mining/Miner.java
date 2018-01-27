package com.prisch.mining;

import com.prisch.StompSessionHolder;
import com.prisch.blockchain.BlockchainProperties;
import com.prisch.blocks.Block;
import com.prisch.blocks.BlockRepository;
import com.prisch.global.Constants;
import com.prisch.global.Settings;
import com.prisch.messages.LocalMessages;
import com.prisch.services.HashService;
import com.prisch.services.KeyService;
import com.prisch.transactions.Coinbase;
import com.prisch.transactions.Transaction;
import com.prisch.transactions.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Transaction.Output output = new Transaction.Output();
        output.setAmount(transactionFees + Constants.COINBASE_REWARD);
        output.setAddress(keyService.getAddress());

        Coinbase coinbase = new Coinbase(Settings.VERSION, proposedHeight, output, hashService);
        transactions.add(0, coinbase);

        return transactions;
    }

    private Block buildBlock(Block previousBlock, List<Transaction> transactions) {
        Block block = new Block();

        block.setVersion(Settings.VERSION);
        block.setHeight(previousBlock.getHeight() + 1);

        block.setTransactions(transactions);

        block.setPreviousHash(previousBlock.getHash());
        block.setProperties(buildProperties());

        return block;
    }

    private Map<String, String> buildProperties() {
        Map<String, String> properties = new HashMap<>();
        return properties;
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
        int checkLength = blockchainProperties.getHashCheck().length();
        String blockHashStart = blockHash.substring(0, checkLength);

        return (blockHashStart.compareTo(blockchainProperties.getHashCheck()) <= 0);
    }
}
