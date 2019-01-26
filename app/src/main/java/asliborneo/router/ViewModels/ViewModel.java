package asliborneo.router.ViewModels;

import asliborneo.router.Filters;


public class ViewModel extends android.arch.lifecycle.ViewModel {

    private boolean mIsSigningIn;
    private Filters mFilters;

    public ViewModel() {
        mIsSigningIn = false;
        mFilters = Filters.getDefault();
    }

    public boolean getIsSigningIn() {
        return mIsSigningIn;
    }

    public void setIsSigningIn(boolean mIsSigningIn) {
        this.mIsSigningIn = mIsSigningIn;
    }

    public Filters getFilters() {
        return mFilters;
    }

    public void setFilters(Filters mFilters) {
        this.mFilters = mFilters;
    }
}
