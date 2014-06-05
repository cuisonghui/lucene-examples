package lucene.examples;

import junit.framework.TestCase;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yozhao on 5/30/14.
 */
public class SelectionTest extends TestCase {
  private IndexReader reader;

  @Override
  public void setUp() throws Exception {
    reader = ExamplesUtil.getIndexReader();
  }

  @Test
  public void testStringSelection() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    // selection of abcde in "string" field
    Query q = new TermQuery(new Term("string", "abcde"));
    TopDocs docs = searcher.search(q, 10);
    assertEquals(2, docs.totalHits);
    assertEquals("5", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("8", searcher.doc(docs.scoreDocs[1].doc).get("id"));

    // selection of all the docs has "string" field
    q = new WildcardQuery(new Term("string", "*"));
    docs = searcher.search(q, 10);
    assertEquals(8, docs.totalHits);

    // selection of all the docs don't have "string" field
    BooleanQuery bQuery = new BooleanQuery();
    MatchAllDocsQuery allDocsQuery = new MatchAllDocsQuery();
    bQuery.add(
        allDocsQuery,
        BooleanClause.Occur.MUST
    );
    bQuery.add(
        new WildcardQuery(new Term("string", "*")),
        BooleanClause.Occur.MUST_NOT
    );
    docs = searcher.search(bQuery, 10);
    assertEquals(2, docs.totalHits);
    assertEquals("7", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("9", searcher.doc(docs.scoreDocs[1].doc).get("id"));

    // search "world" in "text" field then filter with "string" field
    QueryParser parser = new QueryParser(Version.LUCENE_48, "text",
        new StandardAnalyzer(Version.LUCENE_48));
    q = parser.parse("Hello lucene");
    TermQuery tQuery = new TermQuery(new Term("string", "abcd"));
    QueryWrapperFilter filter = new QueryWrapperFilter(tQuery);
    docs = searcher.search(q, filter, 10);
    // return doc 3 and doc 2, doc 3 is first since matching score is higher
    assertEquals(2, docs.totalHits);
    assertEquals("3", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("2", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    reader.close();
  }

  @Test
  public void testLongSelection() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    Query q = NumericRangeQuery.newLongRange("long", 3L, 3L, true, true);
    TopDocs docs = searcher.search(q, 10);
    assertEquals(2, docs.totalHits);
    assertEquals("4", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("6", searcher.doc(docs.scoreDocs[1].doc).get("id"));

    // selection of all the docs has "long" field
    q = NumericRangeQuery.newLongRange("long", Long.MIN_VALUE, Long.MAX_VALUE, true, true);
    docs = searcher.search(q, 10);
    assertEquals(9, docs.totalHits);

    // selection of all the docs don't have "long" field
    BooleanQuery bQuery = new BooleanQuery();
    MatchAllDocsQuery allDocsQuery = new MatchAllDocsQuery();
    bQuery.add(
        allDocsQuery,
        BooleanClause.Occur.MUST
    );
    bQuery.add(
        NumericRangeQuery.newLongRange("long", Long.MIN_VALUE, Long.MAX_VALUE, true, true),
        BooleanClause.Occur.MUST_NOT
    );
    docs = searcher.search(bQuery, 10);
    assertEquals(1, docs.totalHits);
    assertEquals("8", searcher.doc(docs.scoreDocs[0].doc).get("id"));

    // search "world" in "text" field then filter with "long" field
    QueryParser parser = new QueryParser(Version.LUCENE_48, "text",
        new StandardAnalyzer(Version.LUCENE_48));
    q = parser.parse("Hello");
    NumericRangeQuery rQuery = NumericRangeQuery.newLongRange("long", 1L, 1L, true, true);
    QueryWrapperFilter filter = new QueryWrapperFilter(rQuery);
    docs = searcher.search(q, filter, 10);
    assertEquals(2, docs.totalHits);
    assertEquals("0", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("2", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    reader.close();
  }

  @Test
  public void testDoubleSelection() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    Query q = NumericRangeQuery.newDoubleRange("double", 1.23456, 1.23456, true, true);
    TopDocs docs = searcher.search(q, 10);
    assertEquals(3, docs.totalHits);
    assertEquals("0", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("3", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    assertEquals("7", searcher.doc(docs.scoreDocs[2].doc).get("id"));

    // selection of all the docs has "long" field
    q = NumericRangeQuery.newDoubleRange("double", Double.MIN_VALUE, Double.MAX_VALUE, true, true);
    docs = searcher.search(q, 10);
    assertEquals(9, docs.totalHits);

    // selection of all the docs don't have "long" field
    BooleanQuery bQuery = new BooleanQuery();
    MatchAllDocsQuery allDocsQuery = new MatchAllDocsQuery();
    bQuery.add(
        allDocsQuery,
        BooleanClause.Occur.MUST
    );
    bQuery.add(
        NumericRangeQuery.newDoubleRange("double", Double.MIN_VALUE, Double.MAX_VALUE, true, true),
        BooleanClause.Occur.MUST_NOT
    );
    docs = searcher.search(bQuery, 10);
    assertEquals(1, docs.totalHits);
    assertEquals("6", searcher.doc(docs.scoreDocs[0].doc).get("id"));

    // search "world" in "text" field then filter with "long" field
    QueryParser parser = new QueryParser(Version.LUCENE_48, "text",
        new StandardAnalyzer(Version.LUCENE_48));
    q = parser.parse("Hello");
    NumericRangeQuery rQuery = NumericRangeQuery.newDoubleRange("double", 1.23456, 1.23456, true,
        true);
    QueryWrapperFilter filter = new QueryWrapperFilter(rQuery);
    docs = searcher.search(q, filter, 10);
    assertEquals(2, docs.totalHits);
    assertEquals("0", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("3", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    reader.close();
  }

}

