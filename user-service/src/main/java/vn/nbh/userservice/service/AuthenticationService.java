package vn.nbh.userservice.service;

import com.nimbusds.jose.JOSEException;
import vn.nbh.userservice.dto.request.AuthenticationRequest;
import vn.nbh.userservice.dto.request.IntrospectRequest;
import vn.nbh.userservice.dto.request.LogoutRequest;
import vn.nbh.userservice.dto.request.RefreshRequest;
import vn.nbh.userservice.dto.response.AuthenticationResponse;
import vn.nbh.userservice.dto.response.IntrospectResponse;

import java.text.ParseException;

public interface AuthenticationService  {
    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);
    void logout(LogoutRequest logoutRequest);
    AuthenticationResponse refreshToken(RefreshRequest refreshRequest) throws ParseException, JOSEException;
    IntrospectResponse introspect(IntrospectRequest introspectRequest);
}
