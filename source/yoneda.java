import java.util.function.Function;

class Id<A> {
  A value;
  Id(A value) { this.value = value; }

  YonedaId<A> lift() {
    return new YonedaId<A>() {
      <B> Id<B> run(Function<A, B> f) {
        return new Id<B>(f.apply(value));
      }
    };
  }
}

final class One {
  static final One one = new One();
  private One() {}
}

abstract class YonedaId<A> {
  abstract <B> Id<B> run(Function<A, B> f);

  Id<A> lower() {
    return run(x -> x);
  }
}

class Proofs {
  // claim: identity = 1
  interface identity {
    <A> A run(A a);
  }

  interface identity_unit {
    <A> A run(Function<One, A> a);
  }

  interface identity_unit_id {
    <A> Id<A> run(Function<One, A> a);
  }

  static Function<identity, One> identity_equals_1_forwards() {
    Function<identity, identity_unit> step1 =
      f -> new identity_unit() {
        public <A> A run(Function<One, A> a) {
          return a.apply(One.one);
        }
      };

    Function<identity_unit, identity_unit_id> step2 =
      f -> new identity_unit_id() {
        public <A> Id<A> run(Function<One, A> a) {
          return new Id<A>(f.run(a));
        }
      };

    Function<identity_unit_id, Id<One>> step3 =
      f -> new YonedaId<One>() {
        <B> Id<B> run(Function<One, B> g) {
          return f.run(g);
        }
      }.lower();

    Function<Id<One>, One> step4 =
      o -> o.value;

    return step1.andThen(step2).andThen(step3).andThen(step4);
  }

  static Function<One, identity> identity_equals_1_backwards() {
    Function<One, Id<One>> step1 =
      a -> new Id<One>(a);

    Function<Id<One>, identity_unit_id> step2 =
      i -> new identity_unit_id() {
        public <A> Id<A> run(Function<One, A> a) {
          return i.lift().run(a);
        }
      };

    Function<identity_unit_id, identity_unit> step3 =
      f -> new identity_unit() {
        public <A> A run(Function<One, A> a) {
          return f.run(a).value;
        }
      };

    Function<identity_unit, identity> step4 =
      f -> new identity() {
        public <A> A run(A a) {
          return f.run(__ -> a);
        }
      };

    return step1.andThen(step2).andThen(step3).andThen(step4);
  }

}
