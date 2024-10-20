package pov.gate.service.agreement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import pov.gate.core.ISafeDataEntry;
import pov.gate.core.SafeException;
import pov.gate.model.AgreementData;

public class AgreementGateChecker<T> extends ArrayList<T> implements ISafeDataEntry {
    public List<AgreementData> agreementDataList;

    public AgreementGateChecker(List<AgreementData> agreementDataList) {
        this.agreementDataList = agreementDataList;
    }

    @Override
    public String getDataAccessor() {
        return "getAgreementTypes";
    }

    @Override
    public void isValid(Object data) throws SafeException {
        if (data == null) {
            throw new SafeException("Agreement data is null");
        }
        if (agreementDataList == null || agreementDataList.isEmpty()) {
            throw new SafeException("Agreement data is empty");
        }

        String allAgreementTypes = agreementDataList.stream()
                .map(AgreementData::getAgreementType)
                .collect(Collectors.joining(","));

        if (!allAgreementTypes.equals(data)) {
            throw new SafeException("Agreement data not all granted");
        }

        System.out.println("Agreement data validated successfully");
    }
}