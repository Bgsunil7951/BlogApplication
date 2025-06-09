import React, { useState } from "react";
import { axiosInstance } from "../utils";
import { useInfiniteQuery } from "react-query";
import { Loader2, Search, ThumbsUp } from "lucide-react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { getLetterImg } from "../data/MenuItem";

const Home = () => {
  const navigate = useNavigate();

  const [searchParam] = useSearchParams();

  const PAGE_LIMIT = 12;

  const searchQuery = searchParam.get("q") ? searchParam.get("q") : "";

  const [searchText, setSearchText] = useState("");

  const fetchBlogs = async ({ pageParam = 0 }) => {
    const response = await axiosInstance.get(`/blog/public/`, {
      params: {
        q: searchQuery,
        page: pageParam,
        limit: PAGE_LIMIT,
      },
    });
    return {
      ...response.data,
      prevParam: pageParam,
    };
  };

  const {
    data: blogsData,
    isLoading: blogsLoading,
    isError,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
  } = useInfiniteQuery({
    queryKey: ["blogs", searchQuery],
    queryFn: fetchBlogs,
    getNextPageParam: (lastPage) => {
      const prevPage = lastPage.prevParam;
      if (lastPage.blogs.totalPages === 0) {
        return undefined;
      } else if (prevPage === lastPage.blogs.totalPages - 1) {
        return undefined;
      }
      return prevPage + 1;
    },
  });

  const handleSearchClick = (tag) => {
    navigate(`/?q=${searchText.trim()}`);
  };

  const BLOGS_LIST = blogsData?.pages?.reduce((result, page) => {
    return [...result, ...page?.blogs?.content];
  }, []);

  return (
    <section className="w-full min-h-screen max-w-[1200px] mx-auto p-2 font-outfit">
      <h1 className="text-2xl">📰 Blogs That Inform & Inspire</h1>

      <article className="w-full mt-5 float-end p-2 shadow-md rounded-2xl flex justify-start items-center">
        <input
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
          type="text"
          className="flex-1 placeholder:text-[0.8rem] text-[0.9rem]"
          placeholder="Search Blogs eg.#travel ,#tech"
        />
        <span
          onClick={handleSearchClick}
          className="p-1 bg-orange-600 text-white rounded-2xl cursor-pointer"
        >
          <Search size={13} />
        </span>
      </article>

      {BLOGS_LIST?.length === 0 && (
        <article className="w-full h-[70vh] flex flex-col justify-center items-center">
           <img src="https://t3.ftcdn.net/jpg/12/34/98/54/360_F_1234985477_7xMUwpl8ZwXz4HVdyfIKpOYri0Ti3tqw.jpg" className="w-[280px] h-[200px]" alt="" />
          <span className="text-[0.8rem]">No Blogs Found</span>
        </article>
      )}

      <article className="w-full max-w-[600px] mx-auto grid grid-cols-1  gap-4 mt-[100px]">
        {BLOGS_LIST?.map((blog, idx) => {
          return <BlogCard data={blog} key={idx} />;
        })}
      </article>

      {isFetchingNextPage && (
        <div className="w-full mt-2 flex justify-center items-center py-3">
          <Loader2
            className="text-orange-600 animate-spin duration-700 ease-in-out"
            size={20}
          />
        </div>
      )}

      {hasNextPage && (
        <div className="w-full mt-2 flex justify-center items-center py-3">
          <span
            onClick={fetchNextPage}
            className="bg-orange-600 text-white cursor-pointer p-1 rounded-3xl text-[0.6rem]"
          >
            Load More
          </span>
        </div>
      )}
    </section>
  );
};

export default Home;

const BlogCard = ({ data }) => {
  const { img, title, id, content, hashTags, likes, author ,createdAt } = data;

  const { name } = author;

  const [readMore, setReadMore] = useState(false);

  const navigate = useNavigate();

  const handleTagClick = (tag) => {
    navigate(`/?q=${tag.replace("#", "").trim()}`);
  };

  return (
    <article className="p-2 rounded-2xl shadow-sm hover:shadow-md  duration-700 font-outfit">
      <article className="w-full my-4">
        <div className="flex justify-start items-center">
          <img
            src={getLetterImg(name)}
            className="shadow rounded-full w-5 h-5"
            alt=""
          />
          <span className="ms-1 font-semibold">{name}</span>
        </div>
        <div className="text-[0.65rem] text-gray-500">
          Posted on {formatDateTime(createdAt)}
        </div>
      </article>

      <img src={img} className="w-full h-[350px] rounded-md" alt="" />
      <div className="flex w-full justify-end">
        <span className="flex justify-center text-[0.8rem] items-center gap-1">
          <ThumbsUp size={12} className="text-red-600 " strokeWidth={3} />
          {likes}
        </span>
      </div>
      <h1 className="text-2xl whitespace-nowrap overflow-hidden text-ellipsis mt-4">
        {title}
      </h1>
      <p className="text-[0.8rem] text-gray-600 overflow-hidden">
        {!readMore ? content.substring(0, 230) : content}
        {
          content.length>230 && <span
          className="text-[0.6rem] bg-gray-300 p-[2px] px-1 rounded-2xl ml-1 cursor-pointer"
          onClick={() => setReadMore(!readMore)}
        >
          {!readMore ? "Read more" : "Read Less"}
        </span>
        }
      </p>
      <div className="flex flex-wrap gap-2 mt-4">
        {hashTags.split(",").map((tag, idx) => (
          <span
            key={idx}
            onClick={() => handleTagClick(tag)}
            className="rounded-3xl text-[0.7rem] bg-orange-600 text-white cursor-pointer py-[2px] px-2"
          >
            {tag}
          </span>
        ))}
      </div>
    </article>
  );
};


function formatDateTime(isoString) {
  const date = new Date(isoString);

  return date.toLocaleString("en-GB", { 
      day: "2-digit", 
      month: "2-digit", 
      year: "numeric", 
      hour: "2-digit", 
      minute: "2-digit", 
      hour12: true 
  }).replace(",", ""); 
}
