package pov.gate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import pov.biz.service.AcctService;
import pov.biz.service.AgreementService;
import pov.biz.service.SafeSecurityService;
import pov.biz.txn.Mtwtx001Doc;
import pov.biz.txn.Mtwtx001TxService;
import pov.gate.aspect.DataCheckAspect;
import pov.gate.cache.TxSafeCache;
import pov.gate.core.SafeException;
import pov.gate.model.AcctData;
import pov.gate.model.AgreementData;
import pov.gate.model.SecurityData;
import pov.gate.service.account.AccountGateChecker;
import pov.gate.service.account.AccountGate;
import pov.gate.service.agreement.AgreementGateChecker;
import pov.gate.service.agreement.AgreementGate;
import pov.gate.service.security.SecurityGateChecker;
import pov.gate.service.security.SecurityGete;

@SpringBootTest(classes = ServiceGateTest.Config.class)
@ComponentScan(basePackages = "pov.secure")
@ActiveProfiles("dummy")
public class ServiceGateTest {

    @Autowired
    private AccountGate safeAcctService;

    @Autowired
    private SafeSecurityService safeSecurityService;

    @Autowired
    private AgreementGate safeAgreementService;

    @Autowired
    private Mtwtx001TxService txService;

    @Autowired
    private TxSafeCache txSafeCache;

    /**
     * Test configuration
     */
    @Configuration
    @EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
    static class Config {
        @Bean
        public AcctService acctService() {
            return new AcctService();
        }

        @Bean
        public AccountGate safeAcctService(AcctService acctService) {
            return new AccountGate(acctService);
        }

        @Bean
        public SecurityGete securityService() {
            return new SecurityGete();
        }

        @Bean
        public SafeSecurityService safeSecurityService(SecurityGete securityService) {
            return new SafeSecurityService(securityService);
        }

        @Bean
        public AgreementService agreementService() {
            return new AgreementService();
        }

        @Bean
        public AgreementGate safeAgreementService(AgreementService agreementService) {
            return new AgreementGate(agreementService);
        }

        @Bean
        public Mtwtx001TxService mtwtx001TxService() {
            return new Mtwtx001TxService();
        }

        @Bean
        public DataCheckAspect dataCheckAspect() {
            return new DataCheckAspect();
        }

        @Bean
        public TxSafeCache txSafeCache() {
            return new TxSafeCache();
        }
    }

    /**
     * Test method to validate safe data after getting it
     */
    @ParameterizedTest(name = "{index} - {0}")
    @CsvSource({
            "success,       1234567890,OTP,'AGREE1,AGREE2',false",
            "wrong-acct-no, 9909999999,SMS,'AGREE1,AGREE2',true"
    })
    public void validateSafeDataAfterGet(String testName, String acctNo, String securityType, String agreementTypes,
            Boolean expectedException) throws Throwable {
        try {
            // get data
            AccountGateChecker<AcctData> safeAcctListEntry = safeAcctService.getSafeAcctList();
            txSafeCache.addSafeDataEntry(safeAcctListEntry);

            SecurityGateChecker<SecurityData> safeSecurityListEntry = safeSecurityService.getList();
            txSafeCache.addSafeDataEntry(safeSecurityListEntry);

            AgreementGateChecker<AgreementData> safeAgreementListEntry = safeAgreementService.getList();
            txSafeCache.addSafeDataEntry(safeAgreementListEntry);

            // create tx doc
            Mtwtx001Doc txDoc = txService.createTransaction();
            txDoc.setAcctNo(acctNo);
            txDoc.setSecurityType(securityType);
            txDoc.setAgreementTypes(agreementTypes);

            // do service
            txService.doTransaction(txDoc);
        } catch (Throwable e) {
            if (expectedException) {
                Assertions.assertEquals(SafeException.class, e.getCause().getClass());
            } else {
                throw e.getCause();
            }
        }
    }
}
