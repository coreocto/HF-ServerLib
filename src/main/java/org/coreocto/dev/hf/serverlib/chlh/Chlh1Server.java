package org.coreocto.dev.hf.serverlib.chlh;

import org.coreocto.dev.hf.commonlib.sse.chlh.Index;
import org.coreocto.dev.hf.commonlib.util.IBase64;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class Chlh1Server {
    private List<Index> indexes = new ArrayList<>();
    private IBase64 base64 = null;

    public Chlh1Server(IBase64 base64) {
        this.base64 = base64;
    }

    public void saveIndex(Index newIndex) {
        indexes.add(newIndex);
    }

    public List<String> Search(List<String> queryStr) {

        List<String> result = new ArrayList<>();

        int indexSize = indexes.size();

        for (int i = 0; i < indexSize; i++) {
            Index index = indexes.get(i);

            int bfSize = index.getBloomFilters().size();

            innerLoop:
            for (int j = 0; j < bfSize; j++) {
                String valInB64 = index.getBloomFilters().get(j);
                for (int z = 0; z < queryStr.size(); z++) {
                    String qValInB64 = queryStr.get(z);
                    BitSet bitSet = BitSet.valueOf(base64.decodeToByteArray(valInB64));
                    BitSet qBitSet = BitSet.valueOf(base64.decodeToByteArray(qValInB64));
                    bitSet.and(qBitSet);
                    if (bitSet.equals(qBitSet)) {
                        result.add(index.getDocId());
                        break innerLoop;
                    }
                }
            }
        }

        return result;
    }
}
