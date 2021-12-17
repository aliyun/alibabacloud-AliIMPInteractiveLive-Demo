#ifndef __YIELD_HELPER_H
#define __YIELD_HELPER_H

#include <boost/asio.hpp>
#include <boost/asio/spawn.hpp>
#include <functional>

namespace yield_helper
{

/*
// for example

int async_test(boost::asio::yield_context token)
{
    yield_helper::context<int> ctx(token);

    boost::thread([&ctx]() {
        boost::this_thread::sleep_for(boost::chrono::seconds(3));
        ctx.resume(boost::system::error_code(), 9999);
    }).detach();

    return ctx.result();
}

*/

template<typename ReturnType>
struct signature_type
{
    using value = void(boost::system::error_code, ReturnType);
};
template<>
struct signature_type<void>
{
    using value = void(boost::system::error_code);
};

template<typename ReturnType = void,
    typename Handler = boost::asio::yield_context,
    typename Signature = typename signature_type<ReturnType>::value
>
class context
{
public:

    context(boost::asio::yield_context token)
        :init(token),
        ex(boost::asio::get_associated_executor(init.completion_handler)),
        ioc(static_cast<boost::asio::io_context&>(ex.context())),
        work_guard(ex),
        handler(init.completion_handler)
    {

    }

    template<typename ...Args>
    inline void resume(Args... args)
    {
        boost::asio::dispatch(ex,
            std::bind<void>(handler, std::forward<Args>(args)...));
    }
    template<typename ...Args>
    inline void dispatch(Args... args)
    {
        boost::asio::dispatch(ex, std::forward<Args>(args)...);
    }
    template<typename ...Args>
    inline void post(Args... args)
    {
        boost::asio::post(ex, std::forward<Args>(args)...);
    }

    inline ReturnType result()
    {
        return init.result.get();
    }


private:
    context(const context&);
    context& operator=(const context&);

    using HandlerType = BOOST_ASIO_HANDLER_TYPE(Handler, Signature);
    using Executor =
        typename boost::asio::associated_executor<HandlerType>::type;
    boost::asio::async_completion<Handler, Signature> init;

public:
    Executor ex;
    boost::asio::io_context& ioc;
    decltype(init.completion_handler) handler;

private:
    boost::asio::executor_work_guard<Executor> work_guard;
};
}

#endif