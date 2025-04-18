package dev.esoterik.rift.cache;

public interface CacheProvider<K, V> {

  V get(K key);

  void put(K key, V value);

  void remove(K key);

  void clear();

  boolean containsKey(K key);

  boolean isEmpty();

  int size();

  Iterable<K> keys();

  Iterable<V> values();

  Iterable<Entry<K, V>> entries();

  interface Entry<K, V> {
    K getKey();

    V getValue();
  }

  record DefaultEntry<K, V>(K key, V value) implements CacheProvider.Entry<K, V> {

    @Override
    public K getKey() {
      return key;
    }

    @Override
    public V getValue() {
      return value;
    }
  }
}
