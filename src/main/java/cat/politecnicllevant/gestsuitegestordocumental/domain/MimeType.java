package cat.politecnicllevant.gestsuitegestordocumental.domain;

public enum MimeType {
    FOLDER{
        @Override
        public String toString() {
            return "application/vnd.google-apps.folder";
        }
    }
}
