package ru.nordmine.services.old;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nordmine.entities.wiki.RelationValues;
import ru.nordmine.entities.wiki.WikiHeader;
import ru.nordmine.entities.wiki.WikiRelation;
import ru.nordmine.entities.wiki.WikiValue;
import ru.nordmine.parser.ColumnInfo;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@Service("infoboxService")
public class InfoboxParserServiceImpl implements InfoboxParserService {

    private static Logger logger = Logger.getLogger(ParserServiceImpl.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void parseInfobox(String primaryField, List<ColumnInfo> allowedRows) {
        List<WikiRelation> relations = em.createQuery("from WikiRelation r where r.header.text = :header", WikiRelation.class)
                .setParameter("header", primaryField)
                .setFirstResult(5)
                .setMaxResults(5)
                .getResultList();

        for (WikiRelation relation : relations) {
            logger.info(relation.getValue().getText());
            Document document = null;
            try {
                document = ParserServiceImpl.parse("http://ru.wikipedia.org" + relation.getValue().getUrl());
            } catch (DocumentException e) {
                e.printStackTrace();
            }

            for (WikiRelation rel : getAllowedRelationsFromInfobox(document, relation, allowedRows)) {
                logger.info(rel.getHeader().getText() + ": " + rel.getValue().getText() + " " + rel.getValue().getUrl());

                // header save
                List<WikiHeader> headers = em.createQuery("from WikiHeader h where h.text = :text", WikiHeader.class)
                        .setParameter("text", rel.getHeader().getText())
                        .getResultList();

                if (headers.isEmpty()) {
                    em.persist(rel.getHeader());
                } else {
                    rel.setHeader(headers.get(0));
                }

                // value save
                List<WikiValue> values = em.createQuery("from WikiValue v where v.url= :url", WikiValue.class)
                        .setParameter("url", rel.getValue().getUrl())
                        .getResultList();

                if (values.isEmpty()) {
                    em.persist(rel.getValue());
                } else {
                    rel.setValue(values.get(0));
                }

                // relation save
                List<WikiRelation> wikiRelations = em.createQuery("from WikiRelation r where r.header = :header and r.value = :value", WikiRelation.class)
                        .setParameter("header", rel.getHeader())
                        .setParameter("value", rel.getValue())
                        .getResultList();

                WikiRelation selectedRelation;

                if (wikiRelations.isEmpty()) {
                    em.persist(rel);
                    selectedRelation = rel;
                } else {
                    selectedRelation = wikiRelations.get(0);
                }

                // relation values save
                List<RelationValues> relationValuesList = em.createQuery("from RelationValues rv where rv.value = :value and rv.relation = :relation", RelationValues.class)
                        .setParameter("value", relation.getValue())
                        .setParameter("relation", selectedRelation)
                        .getResultList();

                if (relationValuesList.isEmpty()) {
                    RelationValues rv = new RelationValues();
                    rv.setValue(relation.getValue());
                    rv.setRelation(selectedRelation);
                    em.persist(rv);
                }
            }
        }
    }

    private List<WikiRelation> getAllowedRelationsFromInfobox(Document document, WikiRelation primaryRelation, List<ColumnInfo> allowedRows) {
        List<WikiRelation> relations = new ArrayList<WikiRelation>();
        if (document != null) {

            List<Node> infoboxNodes = document.selectNodes("//TABLE[contains(@class, 'infobox')]");
            logger.info("infobox table count: " + infoboxNodes.size());
            if (infoboxNodes.isEmpty()) {
                return relations;
            }

            logger.info(infoboxNodes.get(0).asXML());

            Node fullNameNode = infoboxNodes.get(0).selectSingleNode("CAPTION/node()[1]");
            populateRelation(relations, "полное наименование", fullNameNode.getText(), primaryRelation.getValue().getUrl());

            List<Node> rowsNodes = infoboxNodes.get(0).selectNodes("TBODY/TR");
            logger.info("rows count: " + rowsNodes.size());
            for (Node rowNode : rowsNodes) {
                List<Node> cellNodes = rowNode.selectNodes("TD[not(@align)]");

                if (cellNodes.size() == 2) {
                    String stringValue = cellNodes.get(0).getStringValue();

                    ColumnInfo columnInfo = null;
                    for (ColumnInfo allowedHeader : allowedRows) {
                        if (stringValue.toLowerCase().contains(allowedHeader.getHeaderContains())) {
                            columnInfo = allowedHeader;
                            break;
                        }
                    }

                    if (columnInfo == null) {
                        continue;
                    }

                    logger.info("columnInfo: " + columnInfo.getHeaderContains());

                /*
                if(stringValue.contains("\n")) {

                    for (String s : Splitter.on("\n")
                            .trimResults()
                            .omitEmptyStrings()
                            .split(stringValue)) {
                        headers.add(s);
                    }
                } else {
                    headers.add(stringValue);
                }*/

                    List<Node> selectedNodes = cellNodes.get(1).selectNodes(columnInfo.getXpath());
                    logger.info("selected nodes size: " + selectedNodes.size());
                    for (Node n : selectedNodes) {
                        if (n.getName().equals("A")) {
                            populateRelation(relations, columnInfo.getSaveAs(), n.getText(), n.valueOf("@href"));
                        }
                    }
                }
            }
        }
        return relations;
    }

    private void populateRelation(List<WikiRelation> relations, String saveAs, String text, String url) {
        WikiHeader header = new WikiHeader();
        header.setText(saveAs);

        WikiValue value = new WikiValue();
        value.setText(text);
        value.setUrl(url);

        WikiRelation relation = new WikiRelation();
        relation.setHeader(header);
        relation.setValue(value);

        relations.add(relation);
    }

}
