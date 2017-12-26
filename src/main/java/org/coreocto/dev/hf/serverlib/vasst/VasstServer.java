package org.coreocto.dev.hf.serverlib.vasst;

import org.coreocto.dev.hf.commonlib.util.Registry;
import org.coreocto.dev.hf.commonlib.vasst.bean.RelScore;
import org.coreocto.dev.hf.commonlib.vasst.bean.SearchResult;
import org.coreocto.dev.hf.commonlib.vasst.bean.TermFreq;

import java.io.File;
import java.util.*;

public class VasstServer {

    private Map<String, TermFreq> I = null;
    private Map<String, File> F = null;

    private Registry registry = null;

    public VasstServer(Registry registry) {
        this.registry = registry;
        this.I = new HashMap<>();
        this.F = new HashMap<>();
    }

    public void SaveIndex(String docId, TermFreq terms) {
        I.put(docId, terms);
    }

    public void SaveFile(String docId, File docFile) {
        F.put(docId, docFile);
    }

    public void Remove(String docId) {
        //remove index entry from I
        TermFreq termFreq = I.remove(docId);
        termFreq.getTerms().clear();

        //remove file entry from F
        F.remove(docId);
    }

    public List<RelScore> ScoreCalculate(String encKeyword) {

        Map<String, Double> docTermFreqs = new HashMap<>();    //docIds which contains the given keyword

//        int maxTermFreq = 0;    //this max term freq should refer to the most freq keyword in particular document
        //the below implementation is incorrect and need to be refined

        int docCnt = I.size();
        int matchDocCnt = 0;

        for (Map.Entry<String, TermFreq> entry : I.entrySet()) {
            String docId = entry.getKey();
            TermFreq termFreq = entry.getValue();

            int tf = termFreq.get(encKeyword);    //get current word freq.
            int maxTermFreq = 0;

            if (tf == 0) {    //given term not found in current document
                matchDocCnt++;
                continue;
            }

            for (Map.Entry<String, Integer> findMax : termFreq.getTerms().entrySet()) {
                int newVal = findMax.getValue();
                if (newVal > maxTermFreq) {
                    maxTermFreq = newVal;
                }
            }

            double ntf = tf * 1.0 / maxTermFreq;

            docTermFreqs.put(docId, ntf);
        }

        List<RelScore> result = new ArrayList<>();

        double idf = Math.log(docCnt / matchDocCnt);

        for (Map.Entry<String, Double> scoreEntry : docTermFreqs.entrySet()) {
            String docId = scoreEntry.getKey();
            double ntf = scoreEntry.getValue();
            double tfidf = ntf * idf;

            RelScore score = new RelScore();
            score.setDocId(docId);
            score.setKeyword(encKeyword);
            score.setScore(tfidf);
            result.add(score);
        }

        return result;
    }

    public SearchResult Search(List<String> encKeywords) {
        List<RelScore> docScore = this.ScoreCalculate(encKeywords.get(0));

        Collections.sort(docScore, new Comparator<RelScore>() {
            @Override
            public int compare(RelScore o1, RelScore o2) {
                return new Double(o2.getScore()).compareTo(o1.getScore());
            }
        });

        SearchResult searchResult = new SearchResult();
        searchResult.setRelScores(docScore);
        searchResult.setMatchedCnt(docScore.size());

        return searchResult;
    }
}
