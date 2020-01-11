package acorn.api.integrationtest.services;

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import acorn.api.client.RestClient;
import acorn.api.config.security.SecurityContext;
import acorn.api.session.converters.ProfileConverter;
import acorn.api.session.errors.FailedToRetrieveProfileException;
import acorn.api.session.services.SessionService;
import acorn.api.session.services.SessionServiceImpl;
import acorn.session.management.domain.UserSession;
import acorn.session.management.session.SessionManager;

@Service
@Primary
public class InjectingSessionService implements SessionService {

  @Autowired
  private SecurityContext securityContext;
  @Autowired
  private RestClient restClient;
  @Autowired
  private SessionManager sessionManager;
  @Autowired
  private ProfileConverter profileConverter;

  private SessionServiceImpl delegate;

  private Optional<String> stringIdToAdd = Optional.empty();
  private Optional<Long> longIdToAdd = Optional.empty();
  private Optional<String> maskedIdCreated = Optional.empty();
  private Optional<String> maskedLongIdCreated = Optional.empty();

  @PostConstruct
  public void createDelegate() {

    delegate = new SessionServiceImpl(securityContext, restClient, sessionManager, profileConverter);
  }

  @Override
  public UserSession createSession() throws FailedToRetrieveProfileException {
    final UserSession session = delegate.createSession();
    
    maskedIdCreated = stringIdToAdd.map(id -> session.getMaskedStringId(id));
    stringIdToAdd.ifPresent(x -> saveSession(session));
    
    maskedLongIdCreated = longIdToAdd.map(id -> session.getMaskedLongId(id));
    maskedLongIdCreated.ifPresent(x -> saveSession(session));
    return session;
  }

  public void addStringIdToNextSessionCreated(String id) {

    stringIdToAdd = Optional.of(id);
  }
  
  public void addLongIdToNextSessionCreated(Long id) {

    longIdToAdd = Optional.of(id);
  }

  public String getMaskedStringIdAdded() {

    return maskedIdCreated.get();
  }
  
  public String getMaskedLongIdAdded() {

    return maskedLongIdCreated.get();
  }

  @Override
  public UserSession retrieveSession() throws FailedToRetrieveProfileException {

    return delegate.retrieveSession();
  }

  @Override
  public void saveSession(UserSession session) {

    delegate.saveSession(session);
  }

  @Override
  public void deleteSession() {

    delegate.deleteSession();
  }
}
