package com.prisch.commands;

import com.prisch.StompSessionHolder;
import com.prisch.global.Constants;
import com.prisch.global.Settings;
import com.prisch.services.HashService;
import com.prisch.services.KeyService;
import com.prisch.shell.ShellLineReader;
import com.prisch.transactions.Transaction;
import com.prisch.util.Result;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@ShellComponent
@ShellCommandGroup("Blockchain")
public class TransactionCommands {

    private static final String INTEGER_REGEX = "^-?\\d+$";

    @Autowired private KeyService keyService;
    @Autowired private HashService hashService;
    @Autowired private StompSessionHolder stompSessionHolder;
    @Autowired private ShellLineReader shellLineReader;

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

        System.out.println();

        if (askConfirmation(address.get(), amount.get(), feeAmount.get())) {
            Transaction transaction = buildTransaction(address.get(), amount.get(), feeAmount.get());

            String transactionDisplay = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN))
                                                                     .append(transaction.toJson())
                                                                     .style(AttributedStyle.DEFAULT)
                                                                     .toAnsi();
            System.out.println("\n" + transactionDisplay);

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

        // TODO: Check if we have enough money

        return Result.success(Integer.valueOf(amount));
    }

    private Result<Integer> readFeeAmount() {
        String feeAmount = shellLineReader.readLine("Fee Amount: ");

        if (!feeAmount.matches(INTEGER_REGEX)) {
            return Result.failure("The fee amount provided isn't a valid positive integer.");
        }

        // TODO: Check if we have enough money

        return Result.success(Integer.valueOf(feeAmount));
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

    private Transaction buildTransaction(String address, Integer amount, Integer feeAmount) throws Exception {
        // TODO: Add inputs
        List<Transaction.Input> inputs = new LinkedList<>();
        List<Transaction.Output> outputs = buildOutputs(address, amount);
        Map<String, String> properties = buildProperties();

        String transactionHash = hash(inputs, outputs);
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
        transaction.setProperties(properties);

        return transaction;
    }

    private List<Transaction.Output> buildOutputs(String address, Integer amount) {
        List<Transaction.Output> outputs = new LinkedList<>();

        Transaction.Output paymentOutput = new Transaction.Output();
        paymentOutput.setAddress(address);
        paymentOutput.setAmount(amount);

        outputs.add(paymentOutput);

        // TODO: Add change output

        return outputs;
    }

    private Map<String, String> buildProperties() {
        Map<String, String> properties = new HashMap<>();
        return properties;
    }

    private String hash(List<Transaction.Input> inputs, List<Transaction.Output> outputs) {
        StringBuilder serializationBuilder = new StringBuilder();

        inputs.forEach(in -> serializationBuilder.append(in.getBlockHeight())
                                                 .append(in.getTransactionHash()));

        outputs.forEach(out -> serializationBuilder.append(out.getAddress())
                                                   .append(out.getAmount()));

        String serializedTransaction = serializationBuilder.toString();
        return hashService.hash(serializedTransaction);
    }
}
