package com.prisch.assignments.assignment5;

import com.prisch.assignments.Settings;
import com.prisch.assignments.assignment6.TransactionRepository;
import com.prisch.assignments.assignment8.PropertiesBuilder;
import com.prisch.ignore.StompSessionHolder;
import com.prisch.ignore.blockchain.BlockchainIndex;
import com.prisch.ignore.shell.ShellLineReader;
import com.prisch.ignore.util.Result;
import com.prisch.reference.Constants;
import com.prisch.reference.services.HashService;
import com.prisch.reference.services.KeyService;
import com.prisch.reference.transactions.Transaction;
import com.prisch.reference.transactions.TransactionInputBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.*;

@ShellComponent
@ShellCommandGroup("Blockchain")
public class TransactionCommands {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionCommands.class);

    private static final String INTEGER_REGEX = "^-?\\d+$";

    @Autowired private KeyService keyService;
    @Autowired private HashService hashService;
    @Autowired private StompSessionHolder stompSessionHolder;
    @Autowired private ShellLineReader shellLineReader;
    @Autowired private BlockchainIndex blockchainIndex;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private TransactionInputBuilder transactionInputBuilder;
    @Autowired private PropertiesBuilder propertiesBuilder;

    @ShellMethod("Post a transaction to the blockchain")
    public String postTransaction() throws Exception {
        Result<String> address = readAddress();
        if (!address.isSuccess())
            return address.getFailureMessage();

        Result<Integer> amount = readAmount();
        if (!amount.isSuccess())
            return amount.getFailureMessage();

        Result<Integer> feeAmount = readFeeAmount();
        if (!feeAmount.isSuccess())
            return feeAmount.getFailureMessage();

        Optional<String> sufficientBalanceError = checkForSufficientBalance(amount.get(), feeAmount.get());
        if (sufficientBalanceError.isPresent()) {
            return sufficientBalanceError.get();
        }

        System.out.println();

        if (askConfirmation(address.get(), amount.get(), feeAmount.get())) {
            Transaction transaction = buildTransaction(address.get(), amount.get(), feeAmount.get());

            LOG.info(transaction.toJson());
            stompSessionHolder.getStompSession().send("/app/postTransaction", transaction);
        }
        return null;
    }

    private Availability postTransactionAvailability() {
        if (!keyService.checkKeysExist()) {
            return Availability.unavailable("you do not have a key pair yet (use 'generate-keys' to generate them).");
        }

        if (!stompSessionHolder.isConnected()) {
            return Availability.unavailable("your client is not connected to the epicoin network (use 'connect' to connect).");
        }

        return Availability.available();
    }

    private Result<String> readAddress() {
        String address = shellLineReader.readLine("Receiving address: ");

        if (address.length() != Constants.ADDRESS_LENGTH) {
            return Result.failure("The provided address is invalid: it should contain %d characters.", Constants.ADDRESS_LENGTH);
        }
        return Result.success(address);
    }

    private Result<Integer> readAmount() {
        String amount = shellLineReader.readLine("Amount: ");

        if (!amount.matches(INTEGER_REGEX)) {
            return Result.failure("The amount provided isn't a valid positive integer.");
        }

        return Result.success(Integer.valueOf(amount));
    }

    private Result<Integer> readFeeAmount() {
        String feeAmount = shellLineReader.readLine("Fee Amount: ");

        if (!feeAmount.matches(INTEGER_REGEX)) {
            return Result.failure("The fee amount provided isn't a valid positive integer.");
        }

        return Result.success(Integer.valueOf(feeAmount));
    }

    private Optional<String> checkForSufficientBalance(int transferAmount, int feeAmount) {
        int balance = blockchainIndex.getAddressBalance(keyService.getAddress());
        int transactionAmount = transferAmount + feeAmount;

        if (balance < transactionAmount) {
            final String INSUFFICIENT_BALANCE
                    = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
                                                   .append("INSUFFICIENT BALANCE: ")
                                                   .style(AttributedStyle.DEFAULT)
                                                   .append("You do not have enough epicoin to complete the transaction. ")
                                                   .append("You have ")
                                                   .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
                                                   .append(String.valueOf(balance))
                                                   .style(AttributedStyle.DEFAULT)
                                                   .append(" epicoins and the total of the transfer and fee amounts is ")
                                                   .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
                                                   .append(String.valueOf(transactionAmount))
                                                   .style(AttributedStyle.DEFAULT)
                                                   .append(" epicoins.")
                                                   .toAnsi();

            return Optional.of(INSUFFICIENT_BALANCE);
        }
        return Optional.empty();
    }

    private boolean askConfirmation(String address, Integer amount, Integer feeAmount) {
        final String CONFIRMATION
                = new AttributedStringBuilder().append("Please confirm the following: ")
                                               .append("You want to transfer ")
                                               .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
                                               .append(amount.toString())
                                               .style(AttributedStyle.DEFAULT)
                                               .append(" epicoins to the address ")
                                               .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
                                               .append(address)
                                               .style(AttributedStyle.DEFAULT)
                                               .append(" while paying ")
                                               .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
                                               .append(feeAmount.toString())
                                               .style(AttributedStyle.DEFAULT)
                                               .append(" epicoins in fees.\n")
                                               .append("Confirm by typing 'yes' or press enter to cancel: ")
                                               .toAnsi();

        String response = shellLineReader.readLine(CONFIRMATION);

        return response.equalsIgnoreCase("yes");
    }

    private Transaction buildTransaction(String address, Integer transferAmount, Integer feeAmount) throws Exception {
        List<Transaction.Input> inputs = buildInputs(transferAmount + feeAmount);
        int inputAmount = inputs.stream().mapToInt(Transaction.Input::getAmount).sum();

        List<Transaction.Output> outputs = buildOutputs(address, transferAmount, feeAmount, inputAmount);

        String transactionHash = hash(inputs, outputs, feeAmount);
        String signature = keyService.sign(transactionHash);
        String publicKey = keyService.readPublicKey();

        Transaction transaction = new Transaction();
        transaction.setVersion(Settings.VERSION);
        transaction.setInputs(inputs);
        transaction.setOutputs(outputs);
        transaction.setFeeAmount(feeAmount);
        transaction.setHash(transactionHash);
        transaction.setSignature(signature);
        transaction.setPublicKey(publicKey);
        transaction.setProperties(propertiesBuilder.buildProperties());

        return transaction;
    }

    private List<Transaction.Input> buildInputs(int totalAmount) {
        Collection<Transaction> unclaimedTransactions = transactionRepository.getUnclaimedTransactions();

        List<Transaction.Input> inputs = new LinkedList<>();

        // TODO: [5A]
        // CHOOSE - You can either implement this method yourself, or you can uncomment the line below without needing to do anything further
        //inputs = transactionInputBuilder.buildInputs(totalAmount, unclaimedTransactions);

        // The exact transaction outputs that are used to form the new inputs don't really matter
        // The main thing that is important is that the total input amount, and hence the total of the previous transaction outputs
        // should be at least equal to the amount that needs to be transferred + fees (totalAmount)
        // The unclaimedTransactions collection contains all your output transactions that haven't been used in a block yet.
        // Use it to decide which transaction to include in corresponding input entries.

        return inputs;
    }

    private List<Transaction.Output> buildOutputs(String address, int transferAmount, int feeAmount, int inputAmount) {
        List<Transaction.Output> outputs = new LinkedList<>();

        Transaction.Output paymentOutput = new Transaction.Output();
        paymentOutput.setAddress(address);
        paymentOutput.setAmount(transferAmount);
        outputs.add(paymentOutput);

        if (inputAmount > (transferAmount + feeAmount)) {
            // TODO: [5B]
            // If the inputs exceed the transfer outputs along with the fees then we end up with change
            // This change should be deposited back to the sending address, i.e. your address
            // Make sure that the transaction is consistent after the change is added,
            // i.e. the total of all the inputs is exactly equal to the total of all the outputs
        }

        return outputs;
    }

    private String hash(List<Transaction.Input> inputs, List<Transaction.Output> outputs, int feeAmount) {
        StringBuilder serializationBuilder = new StringBuilder();

        inputs.forEach(in -> serializationBuilder.append(in.getTransactionHash()));

        outputs.forEach(out -> serializationBuilder.append(out.getAddress())
                                                   .append(out.getAmount()));

        serializationBuilder.append(feeAmount);

        String serializedTransaction = serializationBuilder.toString();
        return hashService.hash(serializedTransaction);
    }
}
