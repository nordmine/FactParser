package ru.nordmine.services.old;

public class Client {
/*
    private Document readXmlDocumentFromFile(String path) {
        Document document = null;
        SAXReader reader = new SAXReader();
        try {
            document = reader.read(path);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return document;
    }
*/
    /*
    private String downloadContent(String url) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String responseBody = null;
        try {
            HttpGet httpget = new HttpGet(url);

            logger.debug("executing request " + httpget.getURI());

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            responseBody = httpclient.execute(httpget, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
            System.out.println("----------------------------------------");

        } catch (ClientProtocolException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                logger.error(e);
            }
        }
        return responseBody;
    }
*/
}
