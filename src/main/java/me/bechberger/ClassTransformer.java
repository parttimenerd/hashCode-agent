package me.bechberger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Arrays;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.scopedpool.ScopedClassPoolFactoryImpl;
import javassist.scopedpool.ScopedClassPoolRepositoryImpl;

/** class transformer to add code in static initializer. Cannot be used for retransformations */
public class ClassTransformer implements ClassFileTransformer {
  private final ScopedClassPoolFactoryImpl scopedClassPoolFactory =
      new ScopedClassPoolFactoryImpl();

  @Override
  public byte[] transform(
      Module module,
      ClassLoader loader,
      String className,
      Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain,
      byte[] classfileBuffer) {
    if (className.startsWith("me/bechberger/runtime/HashCodeHandler")
        || className.startsWith(
            "me/bechberger"
                + "/ClassTransformer") || className.startsWith("java/") || className.startsWith("jdk/internal") || className.startsWith("com/sun/")) {
      return classfileBuffer;
    }
    try {
      ClassPool cp =
          scopedClassPoolFactory.create(
              loader, ClassPool.getDefault(), ScopedClassPoolRepositoryImpl.getInstance());
      CtClass cc = cp.makeClass(new ByteArrayInputStream(classfileBuffer));
      if (cc.isFrozen() || cc.isInterface()) {
        return classfileBuffer;
      }
      // classBeingRedefined is null if the class has not yet been defined
      transform(className, cc);
      return cc.toBytecode();
    } catch (CannotCompileException | IOException | RuntimeException | NotFoundException e) {
      e.printStackTrace();
      return classfileBuffer;
    }
  }

  private boolean hasImplementedHashCode(CtClass cc) throws NotFoundException {
      if (Arrays.stream(cc.getDeclaredMethods("hashCode")).anyMatch(m -> {
          try {
              return m.getParameterTypes().length == 0;
          } catch (NotFoundException e) {
              throw new RuntimeException(e);
          }
      })) {
          return true;
      }
      return cc.getSuperclass() != null && !cc.getSuperclass().getName().equals("java.lang.Object") && hasImplementedHashCode(cc);
  }

  private void transform(String className, CtClass cc)
      throws CannotCompileException, NotFoundException {
    // add a hashCode method if not present in the class

    if (!cc.getName().equals("java.lang.Object") && !hasImplementedHashCode(cc)) {
      cc.addMethod(
          CtMethod.make(
              "public int hashCode() { return me.bechberger.runtime.HashCodeHandler.hashCodeImplementation(this); }",
              cc));
    }
    // replace every invocation of System.identityHashCode and Object.hashCode with our
    // implementation
    /*for (CtMethod method : cc.getDeclaredMethods()) {
      method.instrument(
          new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
              if (m.getMethodName().equals("identityHashCode")
                  && m.getClassName().equals("java.lang.System")) {
                m.replace("$_ = me.bechberger.runtime.HashCodeHandler.handleIdentityHashCode($1);");
              } else if (m.getMethodName().equals("hashCode")) {
                m.replace("$_ = me.bechberger.runtime.HashCodeHandler.handleHashCode($0);");
              }
            }
          });
    }*/
  }

  private String formatClassName(String className) {
    return className.replace("/", ".");
  }
}
