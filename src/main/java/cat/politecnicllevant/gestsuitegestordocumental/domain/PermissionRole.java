package cat.politecnicllevant.gestsuitegestordocumental.domain;

public enum PermissionRole {
    OWNER{
        @Override
        public String toString() {
            return "owner";
        }
    },
    READER{
        @Override
        public String toString() {
            return "reader";
        }
    },
    WRITER{
        @Override
        public String toString() {
            return "writer";
        }
    },
    COMMENTER{
        @Override
        public String toString() {
            return "commenter";
        }
    },
    ORGANIZER{
        @Override
        public String toString() {
            return "organizer";
        }
    },
    FILE_ORGANIZER{
        @Override
        public String toString() {
            return "fileOrganizer";
        }
    },
}
