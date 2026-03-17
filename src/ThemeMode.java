public enum ThemeMode {
    DARK,
    LIGHT;

    public ThemeMode toggled() {
        return this == DARK ? LIGHT : DARK;
    }
}
