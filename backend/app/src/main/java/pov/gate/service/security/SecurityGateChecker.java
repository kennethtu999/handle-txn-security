package pov.gate.service.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import pov.gate.core.ISafeDataEntry;
import pov.gate.core.SafeException;
import pov.gate.model.SecurityData;

public class SecurityGateChecker<T> extends ArrayList<T> implements ISafeDataEntry {
    public List<SecurityData> securityDataList;

    public SecurityGateChecker(List<SecurityData> securityDataList) {
        this.securityDataList = securityDataList;
    }

    @Override
    public String getDataAccessor() {
        return "getSecurityType";
    }

    @Override
    public void isValid(Object data) throws SafeException {
        if (data == null) {
            throw new SafeException("Security data is null");
        }
        if (securityDataList == null || securityDataList.isEmpty()) {
            throw new SafeException("Security data is empty");
        }

        Optional<SecurityData> securityDataOpt = securityDataList.stream()
                .filter(securityData -> data.equals(securityData.getSecurityType()))
                .findFirst();
        if (securityDataOpt.isPresent()) {
            System.out.println("Security data found: " + securityDataOpt.get().getSecurityName());
        } else {
            throw new SafeException("Security data not found");
        }
    }
}