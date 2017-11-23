package org.coreocto.dev.hf.serverlib.suise;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coreocto.dev.hf.commonlib.suise.bean.AddTokenResult;
import org.coreocto.dev.hf.commonlib.suise.util.SuiseUtil;
import org.coreocto.dev.hf.commonlib.util.Registry;

public class SuiseServer {

    private Map<String, List<String>> regularIdx = null;
    private Map<String, List<String>> searchTokenIdx = null;
    private Map<String, File> encFileList = null;

    private SuiseUtil suiseUtil = null;

    public SuiseServer(SuiseUtil suiseUtil) {
        this.regularIdx = new HashMap<String, List<String>>();
        this.searchTokenIdx = new HashMap<String, List<String>>();
        this.encFileList = new HashMap<String, File>();

        this.suiseUtil = suiseUtil;
    }

    public void Add(String fileId, File encFile, List<String> c, List<String> x) {

        regularIdx.put(fileId, c);
        for (String xElement : x) {
            List<String> tmpList = searchTokenIdx.get(xElement);
            if (!tmpList.contains(fileId)) {
                tmpList.add(fileId);
            }
        }
        encFileList.put(fileId, encFile);
    }

    public void Add(AddTokenResult addTokenResult, File encFile) {
        String docId = addTokenResult.getId();
        regularIdx.put(docId, addTokenResult.getC());
        for (String xElement : addTokenResult.getX()) {
            List<String> tmpList = searchTokenIdx.get(xElement);
            if (!tmpList.contains(docId)) {
                tmpList.add(docId);
            }
        }
        encFileList.put(docId, encFile);
    }


    public List<String> Search(String searchToken) {

        List<String> iw = null;

        if (searchTokenIdx.containsKey(searchToken)) {
            iw = searchTokenIdx.get(searchToken);
        } else {
            iw = new ArrayList<>();
            for (Map.Entry<String, List<String>> entry : regularIdx.entrySet()) {
                String fileId = entry.getKey();
                List<String> tmpList = entry.getValue();

                int listSize = tmpList.size();

                for (int i = 0; i < listSize; i++) {
                    String ci = tmpList.get(i);

                    // generate random 16 bytes
                    byte[] randomBytes = new byte[16];
                    suiseUtil.setRandomBytes(randomBytes, i);

                    Registry registry = suiseUtil.getRegistry();

                    String randomVal = registry.getBase64().encodeToString(randomBytes);

                    byte[] srhTknBytes = null;

                    try{
                        srhTknBytes = searchToken.getBytes("UTF-8");
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    // split the saved ci into li & ri
                    byte[] li = suiseUtil.H(srhTknBytes, randomBytes);

                    if ((registry.getBase64().encodeToString(li) + randomVal).equals(ci)) {
                        iw.add(fileId);
                    }
                }
            }
            searchTokenIdx.put(searchToken, iw);
        }

        return iw;
    }


    public void Delete(String fileId) {

        regularIdx.remove(fileId);
        for (Map.Entry<String, List<String>> entry : searchTokenIdx.entrySet()) {
            List<String> val = entry.getValue();
            if (val.contains(fileId)) {
                val.remove(fileId);
            }
        }
        encFileList.remove(fileId);
    }
}
