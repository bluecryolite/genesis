import adapter.Web3Sdk;
import logs.FormLog;
import logs.ILog;
import models.BCOSAccount;
import models.FactoringPrepare;
import org.web3j.abi.datatypes.Address;
import org.web3j.crypto.Credentials;
import util.Config;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

public class fmMain extends JDialog {
    private JPanel contentPane;
    private JButton buttonCancel;
    private JTextField txtName;
    private JButton btnHelloWorld;
    private JTextArea txtLog;
    private JButton btnFactorPrepareCreate;
    private JComboBox cmbAccountList;
    private JComboBox cmbSellerList;
    private JComboBox cmbFactorList;
    private JTextField txtExpiredDate;
    private JTextArea txtFactoringContent;
    private JList listNeedSignContract;
    private JButton btnSign;

    public fmMain() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonCancel);

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        btnHelloWorld.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    log.info(currentAccount.getName());
                    log.info(" set new name.");
                    currentAccount.setHelloName(txtName.getText());
                    log.info(" current name is:");
                    log.line(currentAccount.getHelloName());
                    log.line();
                } catch(Exception err) {
                    log.line(err.toString());
                }
            }
        });

        btnFactorPrepareCreate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    log.info(currentAccount.getName());
                    log.info(" create Factoring Prepare.");
                    BCOSAccount seller = (BCOSAccount) cmbSellerList.getItemAt(cmbSellerList.getSelectedIndex());
                    BCOSAccount factor = (BCOSAccount) cmbFactorList.getItemAt(cmbFactorList.getSelectedIndex());
                    BigInteger result = currentAccount.createFactoringPrepare(new Address(seller.getCredendials().getAddress())
                            , new Address(factor.getCredendials().getAddress()), txtFactoringContent.getText(), txtExpiredDate.getText());
                    log.info(" new ID is: ");
                    log.line(result.toString());
                    log.info("    Factoring's count is:");
                    log.line(currentAccount.getFactoringPrepareCount().toString());
                    currentAccount.getBuyerContractList().add(result.intValue());
                    log.line();
                    seller.getNeedSellerSignContractList().add(result.intValue());
                    factor.getNeedFactorSignContractList().add(result.intValue());
                    refreshList(result.intValue());
                } catch(Exception err) {
                    log.line(err.toString());
                }
            }
        });

        btnSign.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedString = listNeedSignContract.getSelectedValue().toString();
                if (!(selectedString.startsWith("as") || selectedString.startsWith("need"))) {
                    try {
                        Integer tid = Integer.parseInt(selectedString);
                        log.info(currentAccount.getName());
                        log.info(" sign Factoring Prepare, tid is: ");
                        log.line(tid.toString());
                        FactoringPrepare fp = currentAccount.getFactoringPrepare(tid);
                        currentAccount.sign(tid, fp.getContent(), fp.getExpireDate());
                        fp = currentAccount.getFactoringPrepare(tid);
                        log.info("    buyer signature:");
                        log.line(fp.getBuyerSignature());
                        log.info("    seller signature:");
                        log.line(fp.getSellerSignature());
                        log.info("    factor signature:");
                        log.line(fp.getFactorSignature());
                        log.line();
                        refreshList(tid);
                    } catch (Exception err) {
                        log.line(err.toString());
                    }
                }
            }
        });

        listNeedSignContract.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    btnSign.setEnabled(false);
                    String selectedString = listNeedSignContract.getSelectedValue().toString();
                    if (!(selectedString.startsWith("as") || selectedString.startsWith("need"))) {
                        Integer tid = Integer.parseInt(selectedString);
                        try {
                            log.info(currentAccount.getName());
                            log.info(" get Factoring Prepare, tid is: ");
                            log.line(tid.toString());
                            FactoringPrepare fp = currentAccount.getFactoringPrepare(tid);
                            log.info("    content:");
                            log.line(fp.getContent());
                            log.info("    expire date:");
                            log.line(fp.getExpireDate());
                            log.info("    buyer signature:");
                            log.line(fp.getBuyerSignature());
                            log.info("    seller signature:");
                            log.line(fp.getSellerSignature());
                            log.info("    factor signature:");
                            log.line(fp.getFactorSignature());
                            log.line();

                            if (currentAccount.getNeedFactorSignContractList().indexOf(tid) >= 0
                                    || currentAccount.getNeedSellerSignContractList().indexOf(tid) >= 0) {
                                btnSign.setEnabled(true);
                            }
                        } catch (Exception err) {
                            log.line(err.toString());
                        }
                    }
                }
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        log = new FormLog(txtLog);
        accountList = new ArrayList<BCOSAccount>();
        for (LinkedHashMap<String, String> entry: Config.getCurrent().getAccounts()) {
            BCOSAccount account = new BCOSAccount(Web3Sdk.getWeb3j(), Credentials.create(entry.get("privateKey"), entry.get("publicKey")));
            account.setName(entry.get("name"));
            accountList.add(account);
            cmbAccountList.addItem(account);
            cmbFactorList.addItem(account);
            cmbSellerList.addItem(account);
        }

        if (accountList.size() > 0) {
            cmbAccountList.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    currentAccount = (BCOSAccount)cmbAccountList.getItemAt(cmbAccountList.getSelectedIndex());
                    refreshList(-1);
                }
            });
        }

        cmbAccountList.setSelectedIndex(0);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        txtExpiredDate.setText(format.format(calendar.getTime()));
        btnSign.setEnabled(false);
     }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void refreshList (Integer tid) {
        btnSign.setEnabled(false);

        List<String> items = new ArrayList<String>();
        if (currentAccount.getNeedSellerSignContractList().size() > 0) {
            items.add("need sign as seller:" );
            for (Integer item: currentAccount.getNeedSellerSignContractList()) {
                items.add(item.toString());
            }
        }

        if (currentAccount.getNeedFactorSignContractList().size() > 0) {
            items.add("need sign as factor:" );
            for (Integer item: currentAccount.getNeedFactorSignContractList()) {
                items.add(item.toString());
            }
        }

        if (currentAccount.getBuyerContractList().size() > 0) {
            items.add("as buyer:" );
            for (Integer item: currentAccount.getBuyerContractList()) {
                items.add(item.toString());
            }
        }

        if (currentAccount.getSellerContractList().size() > 0) {
            items.add("as seller:" );
            for (Integer item: currentAccount.getSellerContractList()) {
                items.add(item.toString());
            }
        }

        if (currentAccount.getFactorContractList().size() > 0) {
            items.add("as factor:" );
            for (Integer item: currentAccount.getFactorContractList()) {
                items.add(item.toString());
            }
        }

        listNeedSignContract.setListData(items.toArray());
        if (tid >= 0) {
            ListModel model = listNeedSignContract.getModel();
            int size = model.getSize();
            String s1 = tid.toString();
            for (int i = 0; i < size; i++) {
                String s = model.getElementAt(i).toString();
                if (s1.equals(s)) {
                    listNeedSignContract.setSelectedIndex(i);
                }
            }
        }
    }

    private ILog log;
    private BCOSAccount currentAccount;
    private List<BCOSAccount> accountList;
}
