package ru.nordmine.services;

import com.google.common.base.Joiner;
import org.apache.log4j.Logger;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nordmine.StringHelper;
import ru.nordmine.entities.morpher.MorpherForms;
import ru.nordmine.entities.raw.RawCity;
import ru.nordmine.entities.wiki.WikiRelation;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service("morpherService")
public class MorpherServiceImpl implements MorpherService {

    private static Logger logger = Logger.getLogger(MorpherService.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public void getWordFormsForCountries() {
        List<WikiRelation> relationList = em.createQuery("from WikiRelation wr where wr.header.text = :header", WikiRelation.class)
                .setParameter("header", "страна")
//                .setMaxResults(1)
                .getResultList();
        logger.info("result count: " + relationList.size());
        Set<String> wordList = new HashSet<String>();
        for (WikiRelation relation : relationList) {
            wordList.add(StringHelper.upperFirstChar(relation.getValue().getText()));
        }
        getFormsForWordList(wordList);
    }

    @Override
    public void getWordFormsForInfoboxCaptions() {
        List<RawCity> rawItems = em.createQuery("from RawCity r", RawCity.class)
                .getResultList();
        Set<String> words = new HashSet<String>();

        for (RawCity rawItem : rawItems) {
            Document d = null;
            try {
                d = DocumentHelper.parseText(rawItem.getRaw());
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            if (d != null) {
                List<Node> rows = d.selectNodes("//info/row");
                String type = d.selectSingleNode("//info").valueOf("@type").toLowerCase();
                if (StringHelper.containsOnlyRussianLetters(type)) {
                    words.add(type);
                }
                boolean afterCoord = false;
                for (Node row : rows) {
                    String caption = row.selectSingleNode("caption").getText().toLowerCase();
                    if (caption.equals("координаты")) {
                        afterCoord = true;
                    }
                    if (StringHelper.containsOnlyRussianLetters(caption)/* && !afterCoord*/) {
                        words.add(caption);
                    }
                }
            }
        }

        String result = Joiner.on("\", \"").join(words);
        logger.info(result);
        getFormsForWordList(words);
    }


    @Override
    public void getFormsForWordList(Set<String> wordList) {
        Document result = DocumentHelper.createDocument();
        Element rootElement = result.addElement("morph");

        for (String word : wordList) {
            Document document = requestWordForms(word);
            Node rootNode = document.selectSingleNode("//node()");
            if (rootNode.getName().equalsIgnoreCase("error")) {
                logger.error(rootNode.asXML());
                break;
            }

            appendWordForms(rootElement, word, rootNode);
        }

        saveWordFormsToXml(result);
    }

    @Override
    @Transactional
    public MorpherForms loadFormsFromStorage(String word) {
        MorpherForms forms = null;
        List<MorpherForms> formsList = em.createQuery("from MorpherForms where singleImen = :word", MorpherForms.class)
                .setParameter("word", word)
                .getResultList();
        if (formsList.isEmpty()) {
            Document document = requestWordForms(word);
            Node rootNode = document.selectSingleNode("//node()");
            if (!rootNode.getName().equalsIgnoreCase("error")) {
                forms = populateMorpherFormsEntity(rootNode, word);
                em.persist(forms);
            } else {
                logger.error(rootNode.asXML());
            }
        } else {
            forms = formsList.get(0);
        }
        return forms;
    }

    private MorpherForms populateMorpherFormsEntity(Node rootNode, String word) {
        MorpherForms entity = new MorpherForms();
        List<Node> nodeList = rootNode.selectNodes("node()");
        for (Node node : nodeList) {
            if (node.getName() != null) {
                String name = node.getName();
                String value = node.getText();

                entity.setSingleImen(word);
                if (name.equals("Р")) {
                    entity.setSingleRod(value);
                }
                if (name.equals("Д")) {
                    entity.setSingleDat(value);
                }
                if (name.equals("В")) {
                    entity.setSingleVin(value);
                }
                if (name.equals("Т")) {
                    entity.setSingleTvor(value);
                }
                if (name.equals("П")) {
                    entity.setSinglePred(value);
                }
                List<Node> subNodes = node.selectNodes("node()");
                if (subNodes.size() > 0) {
                    for (Node n : subNodes) {
                        if (n.getName() != null) {
                            name = n.getName();
                            value = n.getText();
                            if (name.equals("И")) {
                                entity.setMultipleImen(value);
                            }
                            if (name.equals("Р")) {
                                entity.setMultipleRod(value);
                            }
                            if (name.equals("Д")) {
                                entity.setMultipleDat(value);
                            }
                            if (name.equals("В")) {
                                entity.setMultipleVin(value);
                            }
                            if (name.equals("Т")) {
                                entity.setMultipleTvor(value);
                            }
                            if (name.equals("П")) {
                                entity.setMultiplePred(value);
                            }
                        }
                    }
                }
            }
        }
        return entity;
    }

    private void appendWordForms(Element rootElement, String word, Node rootNode) {
        Element nodeElement = rootElement.addElement("node");
        nodeElement.addAttribute("name", word);
        Element i = nodeElement.addElement("И");
        i.setText(word);
        List<Node> nodeList = rootNode.selectNodes("node()");
        for (Node node : nodeList) {
            if (node.getName() != null) {
                Element e = nodeElement.addElement(node.getName());
                e.setText(node.getText());
                List<Node> subNodes = node.selectNodes("node()");
                if (subNodes.size() > 0) {
                    for (Node n : subNodes) {
                        if (n.getName() != null) {
                            Element subEl = e.addElement(n.getName());
                            subEl.setText(n.getText());
                        }
                    }
                }
            }
        }
    }

    private Document requestWordForms(String word) {
        String url = null;
        Document document = null;
        SAXReader reader = new SAXReader();
        try {
            url = "http://morpher.ru/WebService.asmx/GetXml?s=" + URLEncoder.encode(word, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            document = reader.read(url);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        logger.info(url);
        return document;
    }

    private void saveWordFormsToXml(Document result) {
        try {
            File outputFile = new File("/home/boris/wiki/word_forms.xml");
            if (outputFile.exists()) {
                outputFile.delete();
            }
            outputFile.createNewFile();
            XMLWriter output = new XMLWriter(
                    new FileWriter(outputFile));
            output.write(result);
            output.close();
            logger.info("forms saved");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
