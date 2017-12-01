package com.prisch.commands;

import com.prisch.StompSessionHolder;
import com.prisch.global.Constants;
import com.prisch.global.Settings;
import com.prisch.services.HashService;
import com.prisch.services.KeyService;
import com.prisch.transactions.Transaction;
import com.prisch.util.Result;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@ShellComponent
@ShellCommandGroup("Blockchain")
public class PostTransactionCommand {

    private static final String INTEGER_REGEX = "^-?\\d+$";

    @Autowired private KeyService keyService;
    @Autowired private HashService hashService;
    @Autowired private StompSessionHolder stompSessionHolder;

    @ShellMethod("Post a transaction to the blockchain")
    public String postTransaction() throws Exception {
        Optional<String> keyErrorMessage = checkKeysExist();
        if (keyErrorMessage.isPresent())
            return keyErrorMessage.get();

        Optional<String> connected = checkConnected();
        if (connected.isPresent())
            return connected.get();

        Result<String> address = readAddress();
        if (!address.isSuccess())
            return address.getFailureMessage();

        Result<Integer> amount = readAmount();
        if (!amount.isSuccess())
            return amount.getFailureMessage();

        Result<Integer> feeAmount = readFeeAmount();
        if (!feeAmount.isSuccess())
            return feeAmount.getFailureMessage();

        Result<Integer> lockHeight = readLockHeight();
        if (!lockHeight.isSuccess())
            return lockHeight.getFailureMessage();

        System.out.println();

        if (askConfirmation(address.get(), amount.get(), feeAmount.get(), lockHeight.get())) {
            Transaction transaction = buildTransaction(address.get(), amount.get(), feeAmount.get(), lockHeight.get());

            String transactionDisplay = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN))
                                                                     .append(transaction.toJson())
                                                                     .style(AttributedStyle.DEFAULT)
                                                                     .toAnsi();
            System.out.println("\n" + transactionDisplay);

            stompSessionHolder.getStompSession().send("/app/postTransaction", transaction);
        }
        return null;
    }

    private Optional<String> checkKeysExist() {
        if (!Files.exists(Constants.PUBLIC_KEY_PATH)) {
            final String ERROR =
                new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
                                             .append("ERROR: ")
                                             .style(AttributedStyle.DEFAULT)
                                             .append("You need a key pair in order to post a transaction. ")
                                             .append("Use the ")
                                             .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
                                             .append("generate-keys ")
                                             .style(AttributedStyle.DEFAULT)
                                             .append("command to create a key pair first.")
                                             .toAnsi();

            return Optional.of(ERROR);
        }

        return Optional.empty();
    }

    private Optional<String> checkConnected() {
        if (!stompSessionHolder.isConnected()) {
            return Optional.of(new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
                    .append("ERROR: ")
                    .style(AttributedStyle.DEFAULT)
                    .append("You need to be connected to the epicoin network before posting a transaction. ")
                    .append("Use the ")
                    .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
                    .append("connect ")
                    .style(AttributedStyle.DEFAULT)
                    .append("command to connect first.")
                    .toAnsi());
        }

        return Optional.empty();
    }

    private Result<String> readAddress() {
        String address = System.console().readLine("Receiving address: ");
        System.out.println();

        if (address.length() != Constants.ADDRESS_LENGTH) {
            return Result.failure("The provided address is invalid: it should contain %d characters.", Constants.ADDRESS_LENGTH);
        }
        return Result.success(address);
    }

    private Result<Integer> readAmount() {
        String amount = System.console().readLine("Amount: ");
        System.out.println();

        if (!amount.matches(INTEGER_REGEX)) {
            return Result.failure("The amount provided isn't a valid positive integer.");
        }

        // TODO: Check if we have enough money

        return Result.success(Integer.valueOf(amount));
    }

    private Result<Integer> readFeeAmount() {
        String feeAmount = System.console().readLine("Fee Amount: ");
        System.out.println();

        if (!feeAmount.matches(INTEGER_REGEX)) {
            return Result.failure("The fee amount provided isn't a valid positive integer.");
        }

        // TODO: Check if we have enough money

        return Result.success(Integer.valueOf(feeAmount));
    }

    private Result<Integer> readLockHeight() {
        String lockHeight = System.console().readLine("Lock height (current block height is xxx): ");
        System.out.println();

        if (!lockHeight.matches(INTEGER_REGEX)) {
            return Result.failure("The lock height isn't a valid positive integer.");
        }

        // TODO: Check if the lock height > current block height

        return Result.success(Integer.valueOf(lockHeight));
    }

    private boolean askConfirmation(String address, Integer amount, Integer feeAmount, Integer lockHeight) {
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
                                               .append(" epicoins in fees as long as the transaction is processed before block ")
                                               .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
                                               .append(lockHeight.toString())
                                               .style(AttributedStyle.DEFAULT)
                                               .append("\n")
                                               .append("Confirm by typing 'yes' or press enter to cancel: ")
                                               .toAnsi();

        String response = System.console().readLine(CONFIRMATION);
        System.out.println();

        return response.equalsIgnoreCase("yes");
    }

    private Transaction buildTransaction(String address, Integer amount, Integer feeAmount, Integer lockHeight) throws Exception {
        // TODO: Add inputs
        List<Transaction.Input> inputs = new LinkedList<>();
        List<Transaction.Output> outputs = buildOutputs(address, amount);
        Map<String, String> properties = buildProperties(lockHeight);

        String transactionHash = hash(inputs, outputs);
        String signature = keyService.sign(transactionHash);
        String publicKey = keyService.readPublicKey();

        Transaction transaction = new Transaction();
        transaction.setVersion(Settings.VERSION);
        transaction.setInputs(inputs);
        transaction.setOutputs(outputs);
        transaction.setHash(transactionHash);
        transaction.setSignature(signature);
        transaction.setPublicKey(publicKey);
        transaction.setProperties(properties);

        return transaction;
    }

    private List<Transaction.Output> buildOutputs(String address, Integer amount) {
        List<Transaction.Output> outputs = new LinkedList<>();

        Transaction.Output paymentOutput = new Transaction.Output();
        paymentOutput.setAddress(address);
        paymentOutput.setAmount(amount);
        paymentOutput.setIndex(0);

        outputs.add(paymentOutput);

        // TODO: Add change output

        return outputs;
    }

    private Map<String, String> buildProperties(int lockHeight) {
        Map<String, String> properties = new HashMap<>();

        properties.put(Constants.LOCK_HEIGHT_PROP, String.valueOf(lockHeight));

        return properties;
    }

    private String hash(List<Transaction.Input> inputs, List<Transaction.Output> outputs) throws NoSuchAlgorithmException {
        StringBuilder serializationBuilder = new StringBuilder();

        inputs.stream().sorted(Comparator.comparingInt(Transaction.Input::getIndex))
                       .forEach(in -> serializationBuilder.append(in.getBlockHeight())
                                                          .append(in.getTransactionHash())
                                                          .append(in.getIndex()));

        outputs.stream().sorted(Comparator.comparingInt(Transaction.Output::getIndex))
                        .forEach(out -> serializationBuilder.append(out.getIndex())
                                                            .append(out.getAddress())
                                                            .append(out.getAmount()));

        String serializedTransaction = serializationBuilder.toString();
        return hashService.hash(serializedTransaction);
    }
}
