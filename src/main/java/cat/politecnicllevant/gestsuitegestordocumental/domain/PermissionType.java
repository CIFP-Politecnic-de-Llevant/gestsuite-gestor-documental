package cat.politecnicllevant.gestsuitegestordocumental.domain;

public enum PermissionType {
    USER{
        @Override
        public String toString() {
            return "user";
        }
    },
    GROUP{
        @Override
        public String toString() {
            return "group";
        }
    },
    DOMAIN{
        @Override
        public String toString() {
            return "domain";
        }
    },
    ANYONE{
        @Override
        public String toString() {
            return "anyone";
        }
    }
}
