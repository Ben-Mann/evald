package net.benmann.evald.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import net.benmann.evald.PackageTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({ PublicAPITests.class, PackageTests.class })
public class AllTestsSuite {
	//Stub against which to apply the suite annotations.
}
