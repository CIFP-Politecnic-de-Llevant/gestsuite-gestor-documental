package cat.politecnicllevant.gestsuitegestordocumental.service.pdfbox;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import java.util.ArrayList;
import java.util.List;

public class ShowSignature {

    public List<String> getSignerNames(PDDocument document) {
        List<String> names = new ArrayList<>();
        List<PDSignature> signatures = document.getSignatureDictionaries();

        for (PDSignature signature: signatures)
            names.add(signature.getName());

        return names;
    }
}
