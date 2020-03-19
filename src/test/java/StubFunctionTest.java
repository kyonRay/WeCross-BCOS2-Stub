import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import com.webank.wecross.stub.bcos.contract.StubFunction;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.FunctionReturnDecoder;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.junit.Test;

public class StubFunctionTest {
    @Test
    public void newFunctionTest() throws IOException {
        String funcName = "funcName";
        List<String> params = Arrays.asList("aa", "bb", "cc");
        Function function = StubFunction.newFunction(funcName, params);
        String abi = FunctionEncoder.encode(function);
        assertTrue(!abi.isEmpty());
    }

    @Test
    public void convertToStringListTest() throws IOException {
        String funcName = "funcName";
        List<String> params = Arrays.asList("aaa", "bbb", "ccc");
        Function function = StubFunction.newFunction(funcName, params);
        String abi = FunctionEncoder.encode(function);

        List<Type> typeList =
                FunctionReturnDecoder.decode(abi.substring(10), function.getOutputParameters());
        List<String> resultList = StubFunction.convertToStringList(typeList);
        assertTrue(resultList.size() == params.size());
        for (int i = 0; i < params.size(); i++) {
            assertEquals(params.get(i), resultList.get(i));
        }
    }

    @Test
    public void convertToStringListTest1() throws IOException {
        String funcName = "funcName";
        List<String> params = Arrays.asList();
        Function function = StubFunction.newFunction(funcName, params);
        String abi = FunctionEncoder.encode(function);

        List<Type> typeList =
                FunctionReturnDecoder.decode(abi.substring(10), function.getOutputParameters());
        List<String> resultList = StubFunction.convertToStringList(typeList);
        assertTrue(resultList.isEmpty());
    }
}
