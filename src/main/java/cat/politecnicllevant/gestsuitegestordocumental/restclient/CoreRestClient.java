package cat.politecnicllevant.gestsuitegestordocumental.restclient;

import cat.politecnicllevant.gestsuitegestordocumental.dto.FileUploadDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.GrupDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.UsuariDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.google.FitxerBucketDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@FeignClient(name = "core")
public interface CoreRestClient {

    //USUARIS
    @GetMapping("/usuaris/profile/{id}")
    ResponseEntity<UsuariDto> getProfile(@PathVariable("id") String idUsuari) throws Exception;

    @GetMapping("/usuaris/profile-by-email/{id}")
    ResponseEntity<UsuariDto> getUsuariByEmail(@PathVariable("id") String email) throws Exception;

    @GetMapping("/public/usuaris/profile/{id}")
    ResponseEntity<UsuariDto> getPublicProfile(@PathVariable("id") String idUsuari) throws Exception;

    @GetMapping("/usuaris/llistat/actius")
    ResponseEntity<List<UsuariDto>> getUsuarisActius();

    @GetMapping("/usuaris/alumnes-by-codigrup/{cursgrup}")
    ResponseEntity<List<UsuariDto>> getAlumnesByCodiGrup(@PathVariable("cursgrup") String cursGrup);

    //GRUP
    @GetMapping("/grup/getById/{idgrup}")
    ResponseEntity<GrupDto> getById(@PathVariable("idgrup") Long idgrup);

    //FITXER BUCKET
    @GetMapping("/fitxerbucket/{id}")
    ResponseEntity<FitxerBucketDto> getFitxerBucketById(@PathVariable("id") Long idfitxerBucket);

    @PostMapping("/fitxerbucket/save")
    ResponseEntity<FitxerBucketDto> save(@RequestBody FitxerBucketDto fitxerBucket) throws IOException;

    @PostMapping("/fitxerbucket/delete")
    void delete(@RequestBody FitxerBucketDto fitxerBucket);

    @PostMapping("/public/fitxerbucket/uploadlocal")
    ResponseEntity<String> handleFileUpload(@RequestPart(value = "file") final MultipartFile uploadfile) throws IOException;

    @PostMapping("/public/fitxerbucket/uploadlocal2")
    ResponseEntity<String> handleFileUpload2(@RequestBody FileUploadDto uploadfile) throws IOException;

    //GOOGLE STORAGE
    @PostMapping("/googlestorage/uploadobject")
    ResponseEntity<FitxerBucketDto> uploadObject(@RequestParam("objectName") String objectName, @RequestParam("filePath") String filePath, @RequestParam("bucket") String bucket) throws IOException, GeneralSecurityException;


}
