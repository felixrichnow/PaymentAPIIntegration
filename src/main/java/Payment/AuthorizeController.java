package Payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class AuthorizeController {

    @Autowired
    private AuthorizeService authorizeService;

    @Autowired
    private UserService userService;


    @RequestMapping(value = "/authorize", method = RequestMethod.POST)
    public ResponseEntity<Object> authorize(@RequestBody Authorization authorization) {

        if (authorization != null) {

            AuthorizeInfo authorizeInfo = new AuthorizeInfo(authorization.getUserId(),
                    UUID.randomUUID().toString(), authorization.getTxAmount(), authorization.getTxAmountCy(),
                    authorization.getTxId(), authorization.getTxTypeId(), authorization.getTxName(),
                    authorization.getProvider(), authorization.getPspService(), authorization.getOriginTxId(),
                    authorization.getAccountId(), authorization.getAccountHolder(), authorization.getMaskedAccount(),
                    authorization.getPspFee(), authorization.getPspFeeCy(), authorization.getPspFeeBase(),
                    authorization.getPspFeeBaseCy(), authorization.getAttributes());

            //Make a call check if the balance - reserved money - fee >0

            boolean tempbool;

            AuthorizeInfo authorizeInfo1 = authorizeInfo;


            UserInfo userInfo = userService.getUserInfo(authorizeInfo.getUserId());

            //This is pretty weak since it requires float format
            float floatValueTxAmount = Float.valueOf(authorizeInfo.getTxAmount().toString());
            float floatValueBalance = userInfo.getBalance().floatValue();

            //If it's a negative amount, it means it's a withdrawal and we need to check there's enough
            // of a balance for it, so money can be authorized
            if(floatValueTxAmount <0.0){
                if(Math.abs(floatValueTxAmount) < floatValueBalance){
                    authorizeService.putAuthorizationInfo(authorizeInfo);
                    tempbool = true;
                }
                else{
                    tempbool = false;
                }
            }
            else{
                //If it's a positive amount, it will be added so no need to check
                tempbool = true;
                authorizeService.putAuthorizationInfo(authorizeInfo);
            }

            AuthorResponse authorResponse = new AuthorResponse(
                    authorizeInfo1.getUserId(), tempbool, authorizeInfo1.getAuthCode()
            );

            return new ResponseEntity<Object>(authorResponse, HttpStatus.OK);

        }
        //If Authorization is null, empty so no JSON
        else {

            FalseResponse falseResponse = new FalseResponse();

            falseResponse.setUserId(authorization.getUserId());
            falseResponse.setErrCode("123");
            falseResponse.setErrMsg("Empty JSON");
            falseResponse.setSuccess(false);
            return new ResponseEntity<Object>(authorization, HttpStatus.OK);
        }

    }

    @RequestMapping(value = "/authorz", method = RequestMethod.GET)
    public List<AuthorizeInfo> getAllAuthorz() {
        return authorizeService.getAuthorizations();
    }

}
