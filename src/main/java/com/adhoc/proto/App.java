package com.adhoc.proto;

import com.adhoc.proto.entity.Constants;
import com.adhoc.proto.entity.Header;
import com.adhoc.proto.entity.Record;
import com.google.common.base.Preconditions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * App parses a transaction file and returns the following output:
 * total credit amount=X
 * total debit amount=Y
 * autopays started=Z
 * autopays ended=A
 * balance for user B=C
 */
public class App {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    private static final String TRANSACTION_FILE_OPT = "transactionFile";
    private static final String USER_ID_OPT = "userId";
    private static final Options OPTIONS =
        new Options()
            .addOption(
                Option.builder()
                    .required(true)
                    .longOpt(TRANSACTION_FILE_OPT)
                    .desc("File path pointing to the transaction file.")
                    .hasArg(true)
                    .build())
            .addOption(
                Option.builder()
                    .required(false)
                    .longOpt(USER_ID_OPT)
                    .desc("UserId to report balance.")
                    .hasArg(true)
                    .build());

    private static final BigInteger DEFAULT_USER_ID = BigInteger.valueOf(2456938384156277127L);

    public static void main(String[] args) throws ParseException, IOException {
        CommandLine cmd = new DefaultParser().parse(OPTIONS, args);
        Path filePath = Paths.get(cmd.getOptionValue(TRANSACTION_FILE_OPT));
        BigInteger userId = cmd.hasOption(USER_ID_OPT) ?
            new BigInteger(cmd.getOptionValue(USER_ID_OPT)) : DEFAULT_USER_ID;
        try (InputStream inputStream = FileUtils.openInputStream(filePath.toFile())) {
            byte[] headerBytes = new byte[Constants.NUM_HEADER_BYTES];
            int bytesRead = inputStream.read(headerBytes);
            Preconditions.checkState(bytesRead == Constants.NUM_HEADER_BYTES);

            Header header = Header.parse(headerBytes);
            Preconditions.checkState(header.getMagicString().equals(Constants.MAGIC_STRING),
                String.format("MagicString does not match expected. Expected %s. Received %s.",
                    Constants.MAGIC_STRING, header.getMagicString()));

            List<Record> records = new ArrayList<>();
            Record.RecordFactory recordFactory = new Record.RecordFactory(inputStream);
            for (int i = 0; i < header.getNumRecords(); i++) {
                records.add(recordFactory.getRecord());
            }

            byte[] remainingData = IOUtils.readAllBytes(inputStream);
            if (remainingData != null && remainingData.length > 0) {
                System.out.printf("Warning: file not fully consumed. %d bytes were unprocessed.\n",
                    remainingData.length);
            }

            printStatistics(records, userId);
        }
    }

    /**
     * Parses through the record and prints the following fields:
     * 1) total credit amount
     * 2) total debit amount
     * 3) autopays started
     * 4) autopays ended
     * 5) balance for particular user
     *
     * NOTE: total credit, total debit, and user balance is rounded to the nearest hundredth value.
     * Ex: 12.955 would be printed as 12.96. 12.954 would be printed as 12.95.
     * @param records Records to parse
     * @param userId Id of the user whose balance will be displayed.
     */
    private static void printStatistics(List<Record> records, BigInteger userId) {
        double totalCreditAmount = 0;
        double totalDebitAmount = 0;
        double userBalance = 0;
        int numAutopaysStarted = 0;
        int numAutopaysEnded = 0;

        for (Record record : records) {
            double balanceChange = 0;
            switch (record.getRecordType()) {
                case Debit:
                    Preconditions.checkNotNull(record.getDollarAmount(),
                        "dollarAmount must be set for a Debit record type");
                    totalDebitAmount += record.getDollarAmount();
                    balanceChange = -1 * record.getDollarAmount();
                    break;
                case Credit:
                    Preconditions.checkNotNull(record.getDollarAmount(),
                        "dollarAmount must be set for a Credit record type");
                    totalCreditAmount += record.getDollarAmount();
                    balanceChange = record.getDollarAmount();
                    break;
                case StartAutopay:
                    numAutopaysStarted++;
                    break;
                case EndAutopay:
                    numAutopaysEnded++;
                    break;
            }

            if (record.getUserId().equals(userId)) {
                userBalance += balanceChange;
            }
        }
        System.out.printf("total credit amount=%s\n", DECIMAL_FORMAT.format(totalCreditAmount));
        System.out.printf("total debut amount=%s\n", DECIMAL_FORMAT.format(totalDebitAmount));
        System.out.printf("autopays started=%d\n", numAutopaysStarted);
        System.out.printf("autopays ended=%d\n", numAutopaysEnded);
        System.out.printf("balance for user %s=%s\n", userId, DECIMAL_FORMAT.format(userBalance));
    }

}
