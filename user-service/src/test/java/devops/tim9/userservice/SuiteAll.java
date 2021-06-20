package devops.tim9.userservice;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import devops.tim9.userservice.controller.UserControllerTest;



@RunWith(Suite.class)
@SuiteClasses({ UserControllerTest.class})
public class SuiteAll{

}
