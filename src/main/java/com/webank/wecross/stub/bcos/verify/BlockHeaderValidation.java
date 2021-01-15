package com.webank.wecross.stub.bcos.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.ObjectMapperFactory;
import com.webank.wecross.stub.bcos.common.BCOSBlockHeader;
import com.webank.wecross.stub.bcos.uaproof.Signer;
import java.util.*;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlockHeader;
import org.fisco.bcos.web3j.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockHeaderValidation {
    private static final Logger logger = LoggerFactory.getLogger(BlockHeaderValidation.class);

    public static void verifyBlockHeader(
            BCOSBlockHeader bcosBlockHeader, String blockVerifierString) throws WeCrossException {
        List<String> sealerList = getPubKeyInBCOSVerifier(blockVerifierString);

        List<BcosBlockHeader.Signature> signatureList = bcosBlockHeader.getSignatureList();

        if (bcosBlockHeader.getNumber() != 0 && !isSignUnique(signatureList)) {
            logger.error(
                    "Some signature in SignList is not unique, signatureList is {}", signatureList);
            throw new WeCrossException(
                    WeCrossException.ErrorCode.INTERNAL_ERROR,
                    "verifyBlockHeader fail, caused by sign is not unique.");
        }
        String blockHash = bcosBlockHeader.getHash();
        Signer signer = Signer.newSigner(EncryptType.encryptType);
        boolean verifyFlag = false;
        boolean finalizeFlag = true;
        for (BcosBlockHeader.Signature signature : signatureList) {
            for (String sealer : sealerList) {
                String address = Keys.getAddress(sealer);
                byte[] signData = Numeric.hexStringToByteArray(signature.getSignature());
                byte[] hashData = Numeric.hexStringToByteArray(blockHash);
                verifyFlag = signer.verifyByHashData(signData, hashData, address);
                if (verifyFlag) break;
            }
            finalizeFlag = finalizeFlag && verifyFlag;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Verify BCOS BlockHeader enabled, the final verification result is {}",
                    finalizeFlag);
        }
        if (!finalizeFlag) {
            logger.error("VerifyBlockHeader fail!, signatureList is {}", signatureList);
            throw new WeCrossException(
                    WeCrossException.ErrorCode.INTERNAL_ERROR,
                    "verifyBlockHeader fail, caused by verify fail.");
        }
    }

    private static boolean isSignUnique(List<BcosBlockHeader.Signature> signatureList) {
        Set<String> testSet = new HashSet<>();
        boolean testFlag = false;
        for (BcosBlockHeader.Signature signature : signatureList) {
            testFlag = testSet.add(signature.getSignature());
            if (!testFlag) break;
        }
        return testFlag;
    }

    private static List<String> getPubKeyInBCOSVerifier(String blockVerifierString)
            throws WeCrossException {
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        try {
            Objects.requireNonNull(
                    blockVerifierString,
                    "'blockVerifierString' in getPubKeyInBCOSVerifier is null.");
            Map<String, Object> bcosVerifierMapper =
                    objectMapper.readValue(
                            blockVerifierString, new TypeReference<Map<String, Object>>() {});
            List<String> pubKey = (List<String>) bcosVerifierMapper.get("pubKey");
            return pubKey;
        } catch (JsonProcessingException e) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.UNEXPECTED_CONFIG,
                    "Parse Json to BCOSVerifier Error, " + e.getMessage(),
                    e.getCause());
        } catch (Exception e) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.UNEXPECTED_CONFIG,
                    "Read BCOSVerifier Json Error, " + e.getMessage(),
                    e.getCause());
        }
    }
}
