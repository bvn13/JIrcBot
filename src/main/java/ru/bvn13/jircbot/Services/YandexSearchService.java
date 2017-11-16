package ru.bvn13.jircbot.Services;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Service
public class YandexSearchService {

    public static final String ENC = "UTF-8";
    public static final String AND = "&";

    public static class YaPage {

        public static final int ITEMS_PER_PAGE = 10;

        private String keyword;
        private int pageNumber;
        private List<YaItem> yaItems = new ArrayList<YaItem>();

        /**
         * Constructor
         * @param keyword keyword for searching
         * @param pageNumber number of page
         */
        public YaPage(final String keyword, final int pageNumber) {
            this.keyword = keyword;
            this.pageNumber = pageNumber;
        }

        public List<YaItem> getYaItems() {
            return yaItems;
        }

        /**
         * Add one SERP item to collection (page)
         * @param item one SERP item
         */
        public void addYaItem(final YaItem item) {

            final int position = (pageNumber * ITEMS_PER_PAGE) + yaItems.size() + 1;
            item.setPosition(position);
            yaItems.add(item);
        }
    }

    public static class YaItem {

        private int position;
        @Getter
        private String url;
        private String domain;
        @Getter
        private String title;
        @Getter
        private String description = "";
        private String passages = "";

        /**
         * Constructor
         * @param url url of current item
         */
        public YaItem(final String url) {
            this.url = url;
        }

    /* Тут набор getter-ов для приватных полей класса... */

        public void setPosition(final int position) {
            this.position = position;
        }

        public void setDomain(final String domain) {
            this.domain = domain;
        }

        public void setTitle(final String title) {
            this.title = title;
        }

        public void setDescription(final String description) {
            this.description = description;
        }

        public void addPassage(final String passage) {
            passages += passage;
        }

        @Override
        public String toString() {
            return "YaItem{" +
                    "position=" + position +
                    ", url='" + url + '\'' +
                    ", domain='" + domain + '\'' +
                    ", title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", passages='" + passages + '\'' +
                    '}';
        }
    }

    public static class YaHandler extends DefaultHandler {

        private static final String IGNORE_TAG = "hlword";

        private final CharArrayWriter buffer = new CharArrayWriter();
        private YaItem currentItem;
        private YaPage yaPage;

        /**
         * Constructor
         * @param yaPage yandex page that will be filled with SERP items
         */
        public YaHandler(final YaPage yaPage) {
            this.yaPage = yaPage;
        }

        @Override
        public void startElement(
                final String uri,
                final String localName,
                final String qName,
                final Attributes attr
        ) throws SAXException {
            super.startElement(uri, localName, qName, attr);
            if (!IGNORE_TAG.equals(qName)) {
                buffer.reset();
            }
        }

        @Override
        public void endElement(
                final String uri,
                final String localName,
                final String qName
        ) throws SAXException {

            super.endElement(uri, localName, qName);
            if ("error".equals(qName)) {
                throw new IllegalArgumentException("Bad request: " + buffer.toString());
            } else if ("url".equals(qName)) {
                currentItem = new YaItem(buffer.toString());
            } else if ("domain".equals(qName) && currentItem != null) {
                currentItem.setDomain(buffer.toString());
            } else if ("title".equals(qName) && currentItem != null) {
                currentItem.setTitle(clearFromTags(buffer.toString()));
            } else if ("headline".equals(qName) && currentItem != null) {
                currentItem.setDescription(clearFromTags(buffer.toString()));
            } else if ("passage".equals(qName) && currentItem != null) {
                currentItem.addPassage(clearFromTags(buffer.toString()));
            } else if ("group".equals(qName) && currentItem != null) {
                yaPage.addYaItem(currentItem);
            }
        }

        @Override
        public void characters(final char[] chars, final int start, final int length)
                throws SAXException {
            super.characters(chars, start, length);
            buffer.write(chars, start, length);
        }

        /**
         * Clear text from unwanted tags
         * @param text text to clear
         * @return cleared text
         */
        private String clearFromTags(final String text) {
            return text.replaceAll("<" + IGNORE_TAG +">", "")
                    .replaceAll("</" + IGNORE_TAG + ">", "");
        }
    }

    @Getter
    @Setter
    private String user;

    @Getter
    @Setter
    private String key;

    @Getter
    @Setter
    private String url;




    /**
     * Retrieve Yandex.XML response stream via GET request
     * @param query search query
     * @param pageNumber number of search page
     * @return Yandex.XML response stream
     * @throws IOException input/output exception
     */
    public InputStream retrieveResponseViaGetRequest(
            final String query,
            final int pageNumber
    ) throws IOException {

        final StringBuilder address = new StringBuilder(this.url);
        address.append("user=").append(user).append(AND)
                .append("key=").append(key).append(AND)
                .append("query=").append(URLEncoder.encode(query, ENC)).append(AND)
                .append("page=").append(pageNumber);
        final URL url = new URL(address.toString());
        return url.openStream();
    }


    /**
     * Load parsed yandex page from Yandex.XML service
     * @param query query for searching
     * @param pageNumber number of page
     * @return parsed result of searching
     * @throws IOException input/output exception
     * @throws SAXException parsing exception
     */
    public YaPage loadYaPage(final String query, final int pageNumber)
            throws IOException, SAXException {

        final YaPage result = new YaPage(query, pageNumber);
        final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setContentHandler(new YaHandler(result));
        xmlReader.parse(
                new InputSource(
                        this.retrieveResponseViaGetRequest(query, pageNumber)
                )
        );
        return result;
    }


}
