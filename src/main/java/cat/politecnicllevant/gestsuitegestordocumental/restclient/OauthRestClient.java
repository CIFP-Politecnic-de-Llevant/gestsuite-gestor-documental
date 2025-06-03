package cat.politecnicllevant.gestsuitegestordocumental.restclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "oauth")
public interface OauthRestClient {
    @PostMapping("/auth/admin/token")
    ResponseEntity<String> getToken(@RequestBody String password);
}
