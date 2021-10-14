
class ImgCollectionMerge
{

private:

	ImgCollection* ic = nullptr;			// The ImgCollection being searched

	fs::path saveTo;

public:
	ImgCollectionMerge(fs::path set);

public:

	void Append(fs::path set);	// search the ImgCollection for an image
	void Save();

private:


};

